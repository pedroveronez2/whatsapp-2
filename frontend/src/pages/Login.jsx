import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Login.css';

function Login() {
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);
  const [phone, setPhone] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        await api.post('/api/users/register', { phone, name, password });
        setIsRegister(false);
        setError('');
        alert('Usuário criado! Faça login.');
      } else {
        const resp = await api.post('/api/users/login', { phone, password });

        // salva o token PRIMEIRO
        localStorage.setItem('token', resp.data.token);

        // atualiza o header do axios imediatamente
        api.defaults.headers.common['Authorization'] = `Bearer ${resp.data.token}`;

        // agora busca os dados do usuário
        const meResp = await api.get('/api/users/me');
        localStorage.setItem('userId', meResp.data.id);
        localStorage.setItem('userName', meResp.data.name);
        localStorage.setItem('userPhone', meResp.data.phone);

        navigate('/chat');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Erro ao conectar com o servidor');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h2 className="login-title">💬 WhatsApp Clone</h2>
        <h3 className="login-subtitle">{isRegister ? 'Criar conta' : 'Entrar'}</h3>

        <form onSubmit={handleSubmit} className="login-form">
          {isRegister && (
            <input
              className="login-input"
              type="text"
              placeholder="Seu nome"
              value={name}
              onChange={e => setName(e.target.value)}
              required
            />
          )}
          <input
            className="login-input"
            type="tel"
            placeholder="Telefone (ex: 11999999999)"
            value={phone}
            onChange={e => setPhone(e.target.value)}
            required
          />
          <input
            className="login-input"
            type="password"
            placeholder="Senha"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />

          {error && <p className="login-error">{error}</p>}

          <button className="login-button" type="submit" disabled={loading}>
            {loading ? 'Aguarde...' : isRegister ? 'Criar conta' : 'Entrar'}
          </button>
        </form>

        <p className="login-toggle">
          {isRegister ? 'Já tem conta?' : 'Não tem conta?'}{' '}
          <span
            className="login-link"
            onClick={() => { setIsRegister(!isRegister); setError(''); }}
          >
            {isRegister ? 'Entrar' : 'Criar conta'}
          </span>
        </p>
      </div>
    </div>
  );
}

export default Login;