import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getAllTemplates, createTemplate, type Template, deleteTemplate, toggleTemplateStatus } from '../services/template.service';
import { Plus, Trash2, Activity, CheckCircle, XCircle } from 'lucide-react';

const Templates: React.FC = () => {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({ name: '', subject: '', body: '' });
  const [submitError, setSubmitError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchTemplates = async () => {
    setLoading(true);
    try {
      const data = await getAllTemplates();
      setTemplates(data);
    } catch (err) {
      console.error('Failed to fetch templates', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitError('');
    try {
      await createTemplate({
        name: formData.name,
        subject: formData.subject,
        body: formData.body,
        isActive: true,
      });
      setShowModal(false);
      setFormData({ name: '', subject: '', body: '' });
      fetchTemplates();
    } catch (err: any) {
      setSubmitError(err.response?.data?.message || 'Failed to create template');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this template?')) {
      try {
        await deleteTemplate(id);
        fetchTemplates();
      } catch (err) {
        console.error('Failed to delete template', err);
      }
    }
  };

  const handleToggleStatus = async (id: string, currentStatus: boolean) => {
    try {
      await toggleTemplateStatus(id, !currentStatus);
      fetchTemplates();
    } catch (err) {
      console.error('Failed to toggle status', err);
    }
  };

  return (
    <Layout>
      <div className="animate-fade-in" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <h1 style={{ margin: 0 }}>Message Templates</h1>
          <button className="btn-primary" onClick={() => setShowModal(true)} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginRight: '4rem' }}>
            <Plus size={20} /> Create Template
          </button>
        </div>

        {loading ? (
          <div>Loading templates...</div>
        ) : templates.length === 0 ? (
          <div className="glass-panel" style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
             <Activity size={48} style={{ marginBottom: '1rem', opacity: 0.5 }} />
             <h3>No templates found</h3>
             <p>Create your first template to start sending notifications.</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
            {templates.map(template => (
              <div key={template.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                  <div>
                    <h3 style={{ margin: '0 0 0.5rem 0' }}>{template.name}</h3>
                  </div>
                  <button 
                    onClick={() => handleToggleStatus(template.id, template.isActive)}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', color: template.isActive ? '#10b981' : 'var(--text-muted)' }}
                    title={template.isActive ? 'Active' : 'Inactive'}
                  >
                    {template.isActive ? <CheckCircle size={20} /> : <XCircle size={20} />}
                  </button>
                </div>
                
                {template.subject && (
                  <div style={{ fontSize: '0.9rem', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>
                    <strong>Subject:</strong> {template.subject}
                  </div>
                )}
                
                <div style={{ 
                  flex: 1, 
                  background: 'rgba(0,0,0,0.2)', 
                  padding: '1rem', 
                  borderRadius: '8px',
                  fontSize: '0.9rem',
                  fontFamily: 'monospace',
                  whiteSpace: 'pre-wrap',
                  marginBottom: '1rem',
                  overflowY: 'auto',
                  maxHeight: '150px'
                }}>
                  {template.body}
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                  <button onClick={() => handleDelete(template.id)} style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', border: 'none', padding: '0.5rem', borderRadius: '4px', cursor: 'pointer' }}>
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '500px', padding: '2rem' }}>
            <h2 style={{ marginTop: 0, marginBottom: '1.5rem' }}>Create New Template</h2>
            
            {submitError && <div style={{ color: '#ef4444', marginBottom: '1rem' }}>{submitError}</div>}
            
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label className="form-label">Template Name</label>
                <input type="text" className="input-glass" required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} placeholder="e.g. Welcome Email" />
              </div>
              
              <div className="form-group">
                <label className="form-label">Subject (Optional for SMS/Push)</label>
                <input type="text" className="input-glass" value={formData.subject} onChange={e => setFormData({...formData, subject: e.target.value})} placeholder="Welcome to PulseNotify!" />
              </div>

              <div className="form-group">
                <label className="form-label">Content (Use {'{{variable}}'} for dynamic data)</label>
                <textarea 
                  className="input-glass" 
                  required 
                  value={formData.body} 
                  onChange={e => setFormData({...formData, body: e.target.value})} 
                  placeholder="Hi {{name}}, welcome to our platform!"
                  style={{ minHeight: '150px', resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                <button type="button" onClick={() => setShowModal(false)} className="input-glass" style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }}>Cancel</button>
                <button type="submit" className="btn-primary" style={{ flex: 1 }} disabled={isSubmitting}>
                  {isSubmitting ? 'Creating...' : 'Create Template'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Layout>
  );
};

export default Templates;
