import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

let stompClient = null;
let connected = false;
let connecting = false;

export function connectWebSocket(token, userId, onMessage, onPresence) {
    if (connected || connecting) return;
    connecting = true;

    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const headers = {
        Authorization: `Bearer ${token}`,
        userId: userId,
    };

    stompClient.connect(headers, () => {
        console.log('WebSocket conectado!');
        connected = true;
        connecting = false;

        setTimeout(() => {
            if (!stompClient) return;

            stompClient.subscribe(`/topic/messages/${userId}`, (msg) => {
                const body = JSON.parse(msg.body);
                onMessage(body);
            });

            stompClient.subscribe('/topic/presence', (msg) => {
                const body = JSON.parse(msg.body);
                onPresence(body);
            });

            stompClient.send('/app/presence', {}, JSON.stringify({
                userId: String(userId),
                online: 'true',
            }));

        }, 500);

    }, (error) => {
        console.error('Erro WebSocket:', error);
        connected = false;
        connecting = false;
        stompClient = null;
    });
}

export function disconnectWebSocket(userId) {
    if (!stompClient || !connected) return;

    try {
        stompClient.send('/app/presence', {}, JSON.stringify({
            userId: String(userId),
            online: 'false',
        }));
        stompClient.disconnect();
    } catch (e) {
        console.warn('Erro ao desconectar:', e);
    } finally {
        connected = false;
        connecting = false;
        stompClient = null;
    }
}