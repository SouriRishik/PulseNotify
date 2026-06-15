import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { verifyEmail, resendOtp } from '../services/auth.service';
import { Activity } from 'lucide-react';

const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const email = searchParams.get('email') || '';
  const navigate = useNavigate();
  const [otp, setOtp] = useState('');
  const [status, setStatus] = useState<'idle' | 'verifying' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('');
  const [timeLeft, setTimeLeft] = useState(60);

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft]);

  const handleResend = async () => {
    if (timeLeft > 0 || !email) return;
    
    try {
      setStatus('idle');
      setMessage('Resending OTP...');
      await resendOtp(email);
      setTimeLeft(60);
      setMessage('A new OTP has been sent to your email.');
      setStatus('success'); // just to show it as green
    } catch {
      setStatus('error');
      setMessage('Failed to resend OTP.');
    }
  };

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      setStatus('error');
      setMessage('Missing email address. Please try registering again.');
      return;
    }

    setStatus('verifying');
    try {
      await verifyEmail(email, otp);
      setStatus('success');
      setMessage('Your email has been successfully verified! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch {
      setStatus('error');
      setMessage('Verification failed. The code may be expired or invalid.');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', width: '100%' }}>
      <div className="glass-panel animate-fade-in" style={{ padding: '3rem', width: '100%', maxWidth: '450px', textAlign: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '2rem' }}>
          <Activity color="var(--primary)" size={40} style={{ marginRight: '10px' }} />
          <h1 style={{ margin: 0, color: 'var(--primary)' }}>PulseNotify</h1>
        </div>
        
        <h2>Email Verification</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
          We've sent a 6-digit code to <strong>{email}</strong>
        </p>
        
        <form onSubmit={handleVerify}>
          <div className="form-group" style={{ textAlign: 'left' }}>
            <label className="form-label">6-Digit Code</label>
            <input 
              type="text" 
              className="input-glass" 
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              placeholder="e.g. 123456"
              maxLength={6}
              required
              style={{ textAlign: 'center', fontSize: '1.5rem', letterSpacing: '0.5rem' }}
            />
          </div>

          <div style={{ margin: '1rem 0', minHeight: '24px' }}>
            {status === 'verifying' && <p style={{ color: 'var(--text-secondary)', margin: 0 }}>Verifying...</p>}
            {status === 'success' && <p style={{ color: '#10b981', margin: 0 }}>{message}</p>}
            {status === 'error' && <p style={{ color: '#ef4444', margin: 0 }}>{message}</p>}
          </div>

          <button 
            type="submit"
            className="btn-primary" 
            style={{ width: '100%', marginBottom: '1rem' }}
            disabled={status === 'verifying' || status === 'success'}
          >
            Verify Code
          </button>
          
          <div style={{ textAlign: 'center', marginTop: '1rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>Didn't receive the code? </span>
            <button
              type="button"
              onClick={handleResend}
              disabled={timeLeft > 0}
              style={{
                background: 'none',
                border: 'none',
                color: timeLeft > 0 ? 'var(--text-secondary)' : 'var(--primary)',
                cursor: timeLeft > 0 ? 'not-allowed' : 'pointer',
                fontWeight: 'bold',
                padding: 0,
                textDecoration: timeLeft > 0 ? 'none' : 'underline'
              }}
            >
              {timeLeft > 0 ? `Resend in ${timeLeft}s` : 'Resend OTP'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default VerifyEmail;
