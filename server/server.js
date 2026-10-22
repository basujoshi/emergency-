require("dotenv").config();
const express = require("express");
const http = require("http");
const { Server } = require("socket.io");
const crypto = require("crypto");

const app = express();
app.disable("x-powered-by");
app.get("/health", (_req,res)=>res.json({ok:true,service:"bd-mobile-remote-signaling"}));
const server = http.createServer(app);
const allowed = (process.env.WEB_ORIGIN || "https://bashurod.com.np").split(",").map(s=>s.trim());
const io = new Server(server, {
  cors: { origin: allowed, methods: ["GET","POST"] },
  transports: ["websocket"],
  maxHttpBufferSize: 1e6
});

/*
 * DEVELOPMENT SCAFFOLD ONLY.
 * Before public deployment, add real authentication, authorization,
 * per-user rate limiting, abuse monitoring and persistent short-lived
 * pairing tokens. Do not expose this server as a production remote-control
 * endpoint in its current form.
 */
const sessions = new Map(); // code -> {targetId, agentId, accepted, expiresAt}
const sockets = new Map();
function makeCode(){return crypto.randomBytes(4).toString("hex").slice(0,6).toUpperCase()}
function safeText(v,max=800){return typeof v==="string"?v.trim().slice(0,max):""}
function getSession(code){const s=sessions.get(code);if(!s)return null;if(Date.now()>s.expiresAt){sessions.delete(code);return null}return s}
function isMember(s,id){return s && (s.targetId===id || s.agentId===id)}
io.on("connection",socket=>{
  sockets.set(socket.id,{role:null,code:null});
  socket.on("target-create",(_data,ack)=>{
    let code=makeCode();while(sessions.has(code))code=makeCode();
    sessions.set(code,{targetId:socket.id,agentId:null,accepted:false,expiresAt:Date.now()+5*60*1000});
    sockets.set(socket.id,{role:"target",code});
    socket.join(code);
    if(typeof ack==="function")ack({ok:true,code,expiresInSeconds:300});
  });
  socket.on("agent-join",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code);
    if(!s||!s.targetId){socket.emit("pair-result",{ok:false,error:"Pairing code is invalid or expired."});return}
    if(s.agentId&&s.agentId!==socket.id){socket.emit("pair-result",{ok:false,error:"An agent is already requesting this session."});return}
    s.agentId=socket.id;s.accepted=false;s.expiresAt=Date.now()+5*60*1000;
    sockets.set(socket.id,{role:"agent",code});socket.join(code);
    io.to(s.targetId).emit("connection-request",{code,name:safeText(data&&data.name,50)||"Support agent"});
    socket.emit("pair-pending",{code});
  });
  socket.on("target-decision",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code);
    if(!s||s.targetId!==socket.id)return;
    if(data&&data.accept===true&&s.agentId){
      s.accepted=true;s.expiresAt=Date.now()+30*60*1000;
      io.to(s.agentId).emit("pair-result",{ok:true,code});
      socket.emit("pair-result",{ok:true,code});
    }else{
      if(s.agentId)io.to(s.agentId).emit("pair-result",{ok:false,error:"Target declined the session."});
      sessions.delete(code);
    }
  });
  socket.on("chat",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code),text=safeText(data&&data.text,800);
    if(!s||!s.accepted||!isMember(s,socket.id)||!text)return;
    socket.to(code).emit("chat",{code,text,from:sockets.get(socket.id)?.role==="agent"?"Support agent":"Customer"});
  });
  socket.on("remote-gesture",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code);
    if(!s||!s.accepted||s.agentId!==socket.id)return;
    const g=data&&data.gesture;
    if(!g||g.type!=="tap"||!Number.isFinite(g.x)||!Number.isFinite(g.y))return;
    if(g.x<0||g.x>1||g.y<0||g.y>1)return;
    io.to(s.targetId).emit("remote-gesture",{code,gesture:{type:"tap",x:g.x,y:g.y}});
  });
  socket.on("signal",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code);
    if(!s||!s.accepted||!isMember(s,socket.id))return;
    const recipient=socket.id===s.targetId?s.agentId:s.targetId;
    if(recipient)io.to(recipient).emit("signal",{code,kind:safeText(data.kind,20),payload:data.payload});
  });
  socket.on("screen-ready",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code);
    if(s&&s.accepted&&s.targetId===socket.id&&s.agentId)io.to(s.agentId).emit("screen-ready",{code});
  });
  socket.on("end-session",data=>{
    const code=safeText(data&&data.code,8).toUpperCase(),s=getSession(code);
    if(!s||!isMember(s,socket.id))return;
    io.to(code).emit("session-ended",{code});sessions.delete(code);
  });
  socket.on("disconnect",()=>{
    const meta=sockets.get(socket.id);sockets.delete(socket.id);
    if(!meta||!meta.code)return;
    const s=getSession(meta.code);if(!s)return;
    if(s.targetId===socket.id||s.agentId===socket.id){
      io.to(meta.code).emit("session-ended",{code:meta.code});
      sessions.delete(meta.code);
    }
  });
});
const port=Number(process.env.PORT||8080);
server.listen(port,()=>console.log(`BD signaling scaffold listening on ${port}`));
