// src/services/websocket.js
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

let stompClient = null;

export function connectWebSocket(token, userId, onMessage, onPresence) {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);

    const headers = {
        Authorization: `Bearer ${token}`,
        userId: userId,
    };

    stompClient.connect(headers, () => {
        console.log('WebSocket conectado!');

        // avisa que está online
        stompClient.send('/app/presence', {}, JSON.stringify({
            userId: String(userId),
            online: 'true',
        }));

        // inscreve nas mensagens
        stompClient.subscribe(`/topic/messages/${userId}`, (msg) => {
            const body = JSON.parse(msg.body);
            onMessage(body);
        });

        // inscreve na presença
        stompClient.subscribe('/topic/presence', (msg) => {
            const body = JSON.parse(msg.body);
            onPresence(body);
        });
    });
}

export function sendMessage(senderId, receiverId, content) {
    if (!stompClient) return;

    stompClient.send('/app/chat', {}, JSON.stringify({
        senderId: String(senderId),
        receiverId: String(receiverId),
        content,
    }));
}

export function disconnectWebSocket(userId) {
    if (!stompClient) return;

    stompClient.send('/app/presence', {}, JSON.stringify({
        userId: String(userId),
        online: 'false',
    }));

    stompClient.disconnect();
}