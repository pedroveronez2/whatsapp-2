import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

function Login() {
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        await api.post('/api/users/register', { username, password });
        setIsRegister(false);
        setError('');
        alert('Usuário criado! Faça login.');
      } else {
        const resp = await api.post('/api/users/login', { username, password });
        localStorage.setItem('token', resp.data.token);
        localStorage.setItem('username', username);

        // busca o ID do usuário
        const users = await api.get('/api/users/list');
        const me = users.data.find(u => u.username === username);
        localStorage.setItem('userId', me.id);

        navigate('/chat');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Erro ao conectar com o servidor');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.box}>
        <h2 style={styles.title}>💬 WhatsApp Clone</h2>
        <h3 style={styles.subtitle}>{isRegister ? 'Criar conta' : 'Entrar'}</h3>

        <form onSubmit={handleSubmit} style={styles.form}>
          <input
            style={styles.input}
            type="text"
            placeholder="Username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
          <input
            style={styles.input}
            type="password"
            placeholder="Senha"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />

          {error && <p style={styles.error}>{error}</p>}

          <button style={styles.button} type="submit" disabled={loading}>
            {loading ? 'Aguarde...' : isRegister ? 'Criar conta' : 'Entrar'}
          </button>
        </form>

        <p style={styles.toggle}>
          {isRegister ? 'Já tem conta?' : 'Não tem conta?'}{' '}
          <span
            style={styles.link}
            onClick={() => { setIsRegister(!isRegister); setError(''); }}
          >
            {isRegister ? 'Entrar' : 'Criar conta'}
          </span>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    backgroundColor: '#f0f2f5',
  },
  box: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
    width: '100%',
    maxWidth: '380px',
  },
  title: {
    textAlign: 'center',
    color: '#128C7E',
    marginBottom: '4px',
  },
  subtitle: {
    textAlign: 'center',
    color: '#555',
    fontWeight: 'normal',
    marginBottom: '24px',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  input: {
    padding: '12px',
    borderRadius: '8px',
    border: '1px solid #ddd',
    fontSize: '14px',
    outline: 'none',
  },
  button: {
    padding: '12px',
    borderRadius: '8px',
    border: 'none',
    backgroundColor: '#128C7E',
    color: 'white',
    fontSize: '16px',
    cursor: 'pointer',
    marginTop: '8px',
  },
  error: {
    color: 'red',
    fontSize: '13px',
    textAlign: 'center',
  },
  toggle: {
    textAlign: 'center',
    marginTop: '16px',
    fontSize: '14px',
    color: '#555',
  },
  link: {
    color: '#128C7E',
    cursor: 'pointer',
    fontWeight: 'bold',
  },
};

export default Login;