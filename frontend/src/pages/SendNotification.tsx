import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getAllTemplates, type Template } from '../services/template.service';
import { sendNotification } from '../services/notification.service';
import { Send, FileText, User, Calendar, Clock } from 'lucide-react';

const SendNotification: React.FC = () => {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const [recipient, setRecipient] = useState('');
  const [channel, setChannel] = useState('EMAIL');
  const [payloadStr, setPayloadStr] = useState('{}');
  
  const [isScheduled, setIsScheduled] = useState(false);
  const [scheduledAt, setScheduledAt] = useState('');
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<{success: boolean, message: string} | null>(null);

  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        const data = await getAllTemplates();
        setTemplates(data.filter(t => t.isActive));
      } catch (err) {
        console.error('Failed to fetch templates', err);
      } finally {
        setLoading(false);
      }
    };
    fetchTemplates();
  }, []);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setResult(null);

    const template = templates.find(t => t.id === selectedTemplateId);
    if (!template) {
      setResult({ success: false, message: 'Please select a valid template' });
      setIsSubmitting(false);
      return;
    }

    let payload: Record<string, unknown>;
    try {
      payload = JSON.parse(payloadStr);
    } catch {
      setResult({ success: false, message: 'Invalid JSON payload format' });
      setIsSubmitting(false);
      return;
    }

    try {
      await sendNotification({
        channelName: channel,
        recipientAddress: recipient,
        templateId: template.id,
        payload,
        priority: 'HIGH',
        idempotencyKey: crypto.randomUUID(),
        isScheduled,
        scheduledAt: isScheduled && scheduledAt ? new Date(scheduledAt).toISOString() : undefined
      });
      setResult({ success: true, message: 'Notification submitted successfully!' });
      setRecipient('');
      setPayloadStr('{}');
      setSelectedTemplateId('');
    } catch {
      setResult({ success: false, message: 'Failed to send notification' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const selectedTemplate = templates.find(t => t.id === selectedTemplateId);

  return (
    <Layout>
      <div className="animate-fade-in" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <h1 style={{ marginBottom: '2rem' }}>Send Notification</h1>

        {loading ? (
          <div>Loading form...</div>
        ) : templates.length === 0 ? (
          <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center' }}>
            <h3>No active templates found</h3>
            <p>You need to create and activate a template first before sending a notification.</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            
            {/* Form Side */}
            <div className="glass-panel" style={{ padding: '2rem' }}>
              <form onSubmit={handleSend}>
                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <FileText size={16} /> Select Template
                  </label>
                  <select 
                    className="input-glass" 
                    required 
                    value={selectedTemplateId} 
                    onChange={e => setSelectedTemplateId(e.target.value)}
                  >
                    <option value="" disabled>-- Choose a template --</option>
                    {templates.map(t => (
                      <option key={t.id} value={t.id}>{t.name}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Channel</label>
                  <select 
                    className="input-glass" 
                    value={channel} 
                    onChange={e => setChannel(e.target.value)}
                  >
                    <option value="EMAIL">Email</option>
                    <option value="SMS">SMS</option>
                    <option value="PUSH">Push Notification</option>
                    <option value="SLACK">Slack</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <User size={16} /> Recipient Address
                  </label>
                  <input 
                    type="text" 
                    className="input-glass" 
                    required 
                    value={recipient} 
                    onChange={e => setRecipient(e.target.value)} 
                    placeholder={channel === 'EMAIL' ? 'user@example.com' : '+1234567890'} 
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Template Variables (JSON Format)</label>
                  <textarea 
                    className="input-glass" 
                    value={payloadStr} 
                    onChange={e => setPayloadStr(e.target.value)} 
                    style={{ minHeight: '150px', fontFamily: 'monospace' }}
                    placeholder='{"name": "John Doe", "amount": "50.00"}'
                  />
                  <small style={{ color: 'var(--text-secondary)', display: 'block', marginTop: '0.5rem' }}>
                    Provide a valid JSON object matching the {'{{variables}}'} in your template.
                  </small>
                </div>

                <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: isScheduled ? '1rem' : '1.5rem' }}>
                  <input 
                    type="checkbox" 
                    id="isScheduled" 
                    checked={isScheduled} 
                    onChange={e => setIsScheduled(e.target.checked)} 
                    style={{ width: '16px', height: '16px', cursor: 'pointer' }}
                  />
                  <label htmlFor="isScheduled" style={{ color: 'var(--text-main)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Calendar size={16} /> Schedule for later
                  </label>
                </div>

                {isScheduled && (
                  <div className="form-group animate-fade-in">
                    <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Clock size={16} /> Date & Time
                    </label>
                    <input 
                      type="datetime-local" 
                      className="input-glass" 
                      required={isScheduled} 
                      value={scheduledAt} 
                      onChange={e => setScheduledAt(e.target.value)} 
                    />
                  </div>
                )}

                {result && (
                  <div style={{ 
                    padding: '1rem', 
                    borderRadius: '8px', 
                    marginBottom: '1rem',
                    background: result.success ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                    color: result.success ? '#10b981' : '#ef4444',
                    border: `1px solid ${result.success ? '#10b981' : '#ef4444'}`
                  }}>
                    {result.message}
                  </div>
                )}

                <button type="submit" className="btn-primary" style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem' }} disabled={isSubmitting || !selectedTemplateId}>
                  <Send size={20} /> {isSubmitting ? 'Sending...' : 'Send Notification'}
                </button>
              </form>
            </div>

            {/* Preview Side */}
            <div className="glass-panel" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
              <h3 style={{ margin: '0 0 1.5rem 0', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                Preview
              </h3>
              
              {!selectedTemplate ? (
                <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                  Select a template to view preview
                </div>
              ) : (
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                  <div style={{ marginBottom: '1rem' }}>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.2rem' }}>CHANNEL</div>
                    <div style={{ fontWeight: 'bold', color: 'var(--primary)' }}>{channel}</div>
                  </div>
                  
                  {selectedTemplate.subject && (
                    <div style={{ marginBottom: '1rem' }}>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.2rem' }}>SUBJECT</div>
                      <div style={{ padding: '0.5rem', background: 'rgba(0,0,0,0.2)', borderRadius: '4px' }}>{selectedTemplate.subject}</div>
                    </div>
                  )}

                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.2rem' }}>CONTENT TEMPLATE</div>
                    <div style={{ 
                      flex: 1, 
                      padding: '1rem', 
                      background: 'rgba(0,0,0,0.2)', 
                      borderRadius: '8px',
                      whiteSpace: 'pre-wrap',
                      fontFamily: 'monospace'
                    }}>
                      {selectedTemplate.body}
                    </div>
                  </div>
                </div>
              )}
            </div>

          </div>
        )}
      </div>
    </Layout>
  );
};

export default SendNotification;
