import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Bell, Buildings, CaretDown, ChartLineUp, ClipboardText, Cube, GearSix, House, List, MagnifyingGlass, SignOut, Storefront, UsersThree, X } from '@phosphor-icons/react'

type Identity = { userId: string; email: string; role: string }
type LoginResponse = { accessToken: string; user: Identity }
const TOKEN_KEY = 'materialcrm_access_token'
const navigation = [
  { label: 'Visão geral', icon: House },
  { label: 'Vendas', icon: ChartLineUp },
  { label: 'Clientes', icon: UsersThree },
  { label: 'Produtos', icon: Cube },
  { label: 'Estoque', icon: ClipboardText },
]
const notifications = [
  { title: 'Autenticação local', detail: 'A sessão é validada pela API da própria loja.', time: 'agora' },
  { title: 'Próxima entrega', detail: 'Vínculos empresariais e permissões por filial.', time: 'planejado' },
]
async function validateSession(token: string): Promise<Identity> {
  const response = await fetch('/api/v1/me', { headers: { Authorization: `Bearer ${token}` } })
  if (!response.ok) throw new Error('Sessão inválida ou expirada.')
  return response.json() as Promise<Identity>
}
function App() {
  const [token, setToken] = useState<string | null>(null)
  const [identity, setIdentity] = useState<Identity | null>(null)
  const [checking, setChecking] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [branchOpen, setBranchOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  useEffect(() => {
    const saved = sessionStorage.getItem(TOKEN_KEY)
    if (!saved) { setChecking(false); return }
    let active = true
    void validateSession(saved).then((user) => {
      if (active) { setToken(saved); setIdentity(user) }
    }).catch(() => sessionStorage.removeItem(TOKEN_KEY))
      .finally(() => { if (active) setChecking(false) })
    return () => { active = false }
  }, [])
  async function login(email: string, password: string) {
    setLoading(true); setError('')
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      if (response.status === 401) throw new Error('E-mail ou senha inválidos.')
      if (!response.ok) throw new Error('Não foi possível acessar a API local.')
      const result = await response.json() as LoginResponse
      sessionStorage.setItem(TOKEN_KEY, result.accessToken)
      setToken(result.accessToken); setIdentity(result.user)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao entrar no sistema.')
    } finally { setLoading(false) }
  }
  function logout() {
    sessionStorage.removeItem(TOKEN_KEY); setToken(null); setIdentity(null); setError('')
  }
  if (checking) return <main className="auth-loading"><span className="pulse-dot" /> Verificando sessão local...</main>
  if (!token || !identity) return <Login error={error} loading={loading} onSubmit={login} />
  const email = identity.email
  const initials = email.slice(0, 2).toUpperCase()
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div>
      <nav className="sidebar-nav" aria-label="Navegação principal">
        {navigation.map(({ label, icon: Icon }, index) => <button className={`nav-item ${index === 0 ? 'nav-item--active' : ''}`} key={label} type="button" disabled={index !== 0}><Icon size={19} /><span>{label}</span></button>)}
      </nav>
      <div className="sidebar-bottom"><button className="nav-item" type="button" disabled><GearSix size={19} /><span>Configurações</span></button><div className="sidebar-note"><span className="pulse-dot" /> API local autenticada</div></div>
    </aside>
    <main className="workspace">
      <header className="topbar">
        <button className="mobile-menu" type="button" aria-label="Abrir menu"><List size={23} /></button>
        <div className="branch-wrap">
          <button className="branch-select" type="button" onClick={() => setBranchOpen(!branchOpen)} aria-expanded={branchOpen}><span className="branch-icon"><Storefront size={17} /></span><span><small>FILIAL ATIVA</small><strong>Filial ainda não vinculada</strong></span><CaretDown size={16} /></button>
          {branchOpen && <div className="floating-menu branch-menu"><button type="button" disabled><Buildings size={17} /> Nenhuma filial atribuída</button></div>}
        </div>
        <div className="topbar-actions">
          <label className="search"><MagnifyingGlass size={18} /><input placeholder="Buscar no sistema" aria-label="Buscar no sistema" disabled /></label>
          <div className="notification-wrap"><button className="icon-button notification-button" type="button" onClick={() => setNotificationsOpen(!notificationsOpen)} aria-label="Notificações"><Bell size={20} /><span className="notification-count">2</span></button>{notificationsOpen && <Notifications onClose={() => setNotificationsOpen(false)} />}</div>
          <div className="profile-wrap">
            <button className="profile-button" type="button" onClick={() => setProfileOpen(!profileOpen)}><span className="avatar">{initials}</span><span className="profile-name">{email}</span><CaretDown size={15} /></button>
            {profileOpen && <div className="floating-menu profile-menu"><div className="profile-summary"><span className="avatar avatar--large">{initials}</span><span><strong>{email}</strong><small>Conta local · {identity.role}</small></span></div><button type="button" onClick={logout}><SignOut size={18} /> Sair</button></div>}
          </div>
        </div>
      </header>
      <section className="content">
        <div className="page-heading entry-reveal"><div><p className="eyebrow">SESSÃO LOCAL VALIDADA</p><h1>Acesso confirmado.</h1><p>Sua identidade foi autenticada pelo MaterialCRM Local.</p></div><span className="outline-button"><ChartLineUp size={18} /> Fundação técnica</span></div>
        <section className="metric-grid entry-reveal" aria-label="Estado da integração">
          <Metric label="Identidade autenticada" value="Confirmada" detail={email} tone="positive" />
          <Metric label="API local" value="Conectada" detail={`Usuário ${identity.userId.slice(0, 8)}…`} tone="positive" />
          <Metric label="Perfil de acesso" value={identity.role} detail="Permissões por filial serão configuradas depois" tone="warning" />
        </section>
        <section className="dashboard-grid entry-reveal">
          <div className="sales-panel panel"><div className="panel-header"><div><p className="eyebrow">PRÓXIMA ENTREGA</p><h2>Preparação da operação</h2></div></div><div className="setup-steps">
            <div className="setup-step setup-step--done"><span>01</span><div><strong>PostgreSQL local</strong><small>Banco operacional no servidor da loja</small></div></div>
            <div className="setup-step setup-step--done"><span>02</span><div><strong>Autenticação local</strong><small>Senha protegida e token JWT local</small></div></div>
            <div className="setup-step"><span>03</span><div><strong>Vínculo de empresa e filial</strong><small>Próximo controle de autorização</small></div></div>
            <div className="setup-step"><span>04</span><div><strong>Dashboard com dados reais</strong><small>Depois da autorização empresarial</small></div></div>
          </div></div>
          <div className="attention-panel panel"><div className="panel-header"><div><p className="eyebrow">ACESSO</p><h2>Ambiente protegido</h2></div></div><div className="access-note"><span className="pulse-dot" /><p>As áreas comerciais permanecem bloqueadas até a conclusão das permissões por empresa e filial.</p></div><button className="text-button signout-inline" type="button" onClick={logout}>Encerrar sessão <SignOut size={16} /></button></div>
        </section>
      </section>
    </main>
  </div>
}
function Metric({ label, value, detail, tone }: { label: string; value: string; detail: string; tone: string }) {
  return <article className="metric"><p>{label}</p><strong>{value}</strong><span className={`metric-detail metric-detail--${tone}`}>{detail}</span></article>
}
function Notifications({ onClose }: { onClose: () => void }) {
  return <div className="notification-panel floating-menu"><div className="notification-heading"><div><p className="eyebrow">CENTRO DE ATUALIZAÇÕES</p><h2>Notificações</h2></div><button className="icon-button" type="button" onClick={onClose} aria-label="Fechar notificações"><X size={18} /></button></div>{notifications.map((item) => <article className="notification-item" key={item.title}><span className="notification-dot" /><div><strong>{item.title}</strong><p>{item.detail}</p><small>{item.time}</small></div></article>)}</div>
}
function Login({ error, loading, onSubmit }: { error: string; loading: boolean; onSubmit: (email: string, password: string) => Promise<void> }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); void onSubmit(email.trim(), password) }
  return <main className="login-layout">
    <section className="login-intro"><div className="brand"><span className="brand-mark">M</span><span>MaterialCRM</span></div><div><p className="eyebrow">GESTÃO LOCAL PARA QUEM CONSTRÓI</p><h1>Uma operação clara começa com decisões visíveis.</h1><p>Vendas, estoque e relacionamento comercial em um sistema preparado para a rede da sua loja.</p></div><span className="login-status"><span className="pulse-dot" /> Autenticação no servidor local</span></section>
    <section className="login-form-area"><form className="login-form" onSubmit={submit}><p className="eyebrow">ACESSO SEGURO</p><h2>Entrar no MaterialCRM</h2><p>Use a conta de funcionário cadastrada neste servidor.</p>
      <label>E-mail<input required type="email" autoComplete="username" placeholder="nome@empresa.com" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
      <label>Senha<input required type="password" autoComplete="current-password" placeholder="Sua senha" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
      {error && <p className="form-error" role="alert">{error}</p>}
      <button className="primary-button" type="submit" disabled={loading}>{loading ? 'Validando acesso...' : 'Entrar no sistema'}</button><small>Cadastro público desativado. Contas são provisionadas por administrador.</small>
    </form></section>
  </main>
}
export default App
