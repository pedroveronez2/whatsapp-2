import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { connectWebSocket, disconnectWebSocket } from '../services/websocket';
import './Chat.css';

function Chat() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [recording, setRecording] = useState(false);
  const [audioBlob, setAudioBlob] = useState(null);
  const [expandedMessages, setExpandedMessages] = useState({});
  const [unread, setUnread] = useState({});
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const messagesEndRef = useRef(null);
  const selectedUserRef = useRef(null);

  const myId = localStorage.getItem('userId');
  const myName = localStorage.getItem('userName');
  const myPhone = localStorage.getItem('userPhone');
  const token = localStorage.getItem('token');

  // sincroniza o ref com o state
  useEffect(() => {
    selectedUserRef.current = selectedUser;
  }, [selectedUser]);

  useEffect(() => {
    loadUsers();
    connectWebSocket(
      token,
      myId,
      (msg) => {
        setMessages(prev => [...prev, { ...msg, fromMe: false }]);

        // incrementa contador se não está conversando com esse usuário
        setUnread(prev => {
          const senderId = String(msg.senderId);
          const isCurrentChat = selectedUserRef.current &&
            String(selectedUserRef.current.id) === senderId;

          if (isCurrentChat) return prev;

          return {
            ...prev,
            [senderId]: (prev[senderId] || 0) + 1
          };
        });
      },
      (presence) => {
        setUsers(prev => prev.map(u => {
          if (String(u.id) === String(presence.userId)) {
            return { ...u, online: presence.online === 'true' };
          }
          return u;
        }));
      }
    );

    return () => disconnectWebSocket(myId);
  }, []);

  useEffect(() => {
    if (selectedUser) loadHistory();
  }, [selectedUser]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // sincroniza selectedUser com o status atualizado da lista
  useEffect(() => {
    if (selectedUser) {
      const updated = users.find(u => u.id === selectedUser.id);
      if (updated && updated.online !== selectedUser.online) {
        setSelectedUser(updated);
      }
    }
  }, [users]);

  // polling a cada 10 segundos para atualizar status online/offline
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const resp = await api.get('/api/users/list');
        setUsers(resp.data.filter(u => u.phone !== myPhone));
      } catch (e) {
        console.warn('Erro ao atualizar lista:', e);
      }
    }, 10000);

    return () => clearInterval(interval);
  }, []);

  async function loadUsers() {
    const resp = await api.get('/api/users/list');
    setUsers(resp.data.filter(u => u.phone !== myPhone));
  }

  async function loadHistory() {
    const resp = await api.get(`/api/messages/${myId}/${selectedUser.id}`);
    const history = resp.data.map(m => ({
      ...m,
      fromMe: String(m.sender.id) === String(myId),
      senderUsername: m.sender.name,
      type: m.type || 'TEXT',
      content: m.content || '',
      mediaUrl: m.mediaUrl || null,
    }));
    setMessages(history);
  }

  function toggleExpand(index) {
    setExpandedMessages(prev => ({ ...prev, [index]: !prev[index] }));
  }

  function handleSelectUser(user) {
    setSelectedUser(user);
    setUnread(prev => ({ ...prev, [String(user.id)]: 0 }));
  }

  async function handleSendText() {
    if (!content.trim() || !selectedUser) return;

    try {
      await api.post('/api/messages/send', {
        receiverId: parseInt(selectedUser.id),
        content,
      });

      setMessages(prev => [...prev, {
        content,
        type: 'TEXT',
        fromMe: true,
        senderUsername: myName,
        sentAt: new Date().toISOString(),
      }]);
      setContent('');
    } catch (err) {
      console.error('Erro ao enviar mensagem:', err);
      alert('Erro ao enviar mensagem!');
    }
  }

  async function handleSendImage() {
    if (!imageFile || !selectedUser) return;

    const formData = new FormData();
    formData.append('senderId', myId);
    formData.append('receiverId', selectedUser.id);
    formData.append('file', imageFile);

    const resp = await api.post('/api/media/image', formData);
    setMessages(prev => [...prev, {
      type: 'IMAGE',
      mediaUrl: resp.data.mediaUrl,
      fromMe: true,
      senderUsername: myName,
      sentAt: new Date().toISOString(),
    }]);
    setImageFile(null);
  }

  async function startRecording() {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorderRef.current = new MediaRecorder(stream);
    audioChunksRef.current = [];

    mediaRecorderRef.current.ondataavailable = (e) => audioChunksRef.current.push(e.data);
    mediaRecorderRef.current.onstop = () => {
      const blob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
      setAudioBlob(blob);
    };

    mediaRecorderRef.current.start();
    setRecording(true);
  }

  function stopRecording() {
    mediaRecorderRef.current.stop();
    setRecording(false);
  }

  async function handleSendAudio() {
    if (!audioBlob || !selectedUser) return;

    const formData = new FormData();
    formData.append('senderId', myId);
    formData.append('receiverId', selectedUser.id);
    formData.append('file', audioBlob, 'audio.webm');

    const resp = await api.post('/api/media/audio', formData);
    setMessages(prev => [...prev, {
      type: 'AUDIO',
      mediaUrl: resp.data.mediaUrl,
      fromMe: true,
      senderUsername: myName,
      sentAt: new Date().toISOString(),
    }]);
    setAudioBlob(null);
  }

  async function handleLogout() {
    try {
      await api.post(`/api/users/logout/${myId}`);
    } catch (e) {
      console.warn('Erro ao fazer logout:', e);
    } finally {
      disconnectWebSocket(myId);
      localStorage.clear();
      navigate('/login');
    }
  }

  function renderMessage(msg, index) {
    const isMe = msg.fromMe;
    const isLong = msg.content && msg.content.length > 300;
    const isExpanded = expandedMessages[index];

    return (
      <div
        key={index}
        className={`message-bubble ${isMe ? 'message-mine' : 'message-theirs'}`}
      >
        {!isMe && <p className="sender-name">{msg.senderUsername}</p>}

        {msg.type === 'TEXT' && (
          <>
            <p className="message-text">
              {isLong && !isExpanded
                ? msg.content.substring(0, 300) + '...'
                : msg.content}
            </p>
            {isLong && (
              <span className="toggle-text" onClick={() => toggleExpand(index)}>
                {isExpanded ? 'Ver menos ▲' : 'Ver mais ▼'}
              </span>
            )}
          </>
        )}

        {msg.type === 'IMAGE' && (
          <img
            src={`http://localhost:8080${msg.mediaUrl}`}
            alt="imagem"
            className="message-image"
          />
        )}

        {msg.type === 'AUDIO' && (
          <audio
            controls
            src={`http://localhost:8080${msg.mediaUrl}`}
            className="message-audio"
          />
        )}

        <p className="message-time">
          {msg.sentAt ? new Date(msg.sentAt).toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
          }) : ''}
        </p>
      </div>
    );
  }

  return (
    <div className="chat-container">
      <div className="sidebar">
        <div className="sidebar-header">
          <span className="my-name">💬 {myName}</span>
          <button className="logout-btn" onClick={handleLogout}>Sair</button>
        </div>

        <div className="user-list">
          {users.map(user => (
            <div
              key={user.id}
              className={`user-item ${selectedUser?.id === user.id ? 'user-item-active' : ''}`}
              onClick={() => handleSelectUser(user)}
            >
              <div className="user-avatar">{user.name[0].toUpperCase()}</div>
              <div className="user-info">
                <p className="user-name">{user.name}</p>
                <p className="user-status">{user.online ? '🟢 online' : '⚪ offline'}</p>
              </div>
              {unread[String(user.id)] > 0 && (
                <span className="unread-badge">{unread[String(user.id)]}</span>
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="chat-area">
        {selectedUser ? (
          <>
            <div className="chat-header">
              <div className="user-avatar">{selectedUser.name[0].toUpperCase()}</div>
              <div>
                <p className="user-name">{selectedUser.name}</p>
                <p className="user-status">{selectedUser.online ? '🟢 online' : '⚪ offline'}</p>
              </div>
            </div>

            <div className="messages">
              {messages.map((msg, i) => renderMessage(msg, i))}
              <div ref={messagesEndRef} />
            </div>

            <div className="input-area">
              <input
                className="text-input"
                placeholder="Digite uma mensagem..."
                value={content}
                onChange={e => setContent(e.target.value)}
                onKeyPress={e => e.key === 'Enter' && handleSendText()}
              />
              <button className="send-btn" onClick={handleSendText}>➤</button>

              <label className="media-btn">
                🖼️
                <input
                  type="file"
                  accept="image/*"
                  style={{ display: 'none' }}
                  onChange={e => setImageFile(e.target.files[0])}
                />
              </label>
              {imageFile && (
                <button className="send-btn" onClick={handleSendImage}>Enviar img</button>
              )}

              {!recording ? (
                <button className="media-btn" onClick={startRecording}>🎤</button>
              ) : (
                <button className="media-btn recording" onClick={stopRecording}>⏹</button>
              )}
              {audioBlob && !recording && (
                <button className="send-btn" onClick={handleSendAudio}>Enviar áudio</button>
              )}
            </div>
          </>
        ) : (
          <div className="no-chat">
            <p>👈 Selecione um contato para começar a conversar</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default Chat;