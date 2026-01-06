import { useState } from 'react'
import DataForgeApp from './DataForgeApp'
import LoginPage from './LoginPage'
import { useAutoLogout } from './hooks/useAutoLogout'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  // Handle login
  const handleLogin = () => {
    setIsAuthenticated(true)
  }

  // Handle logout (triggered by auto-logout or manual)
  const handleLogout = () => {
    setIsAuthenticated(false)
  }

  // Auto-logout after 15 minutes of inactivity (900000 ms)
  useAutoLogout({
    timeout: 900000, // 15 minutes
    onLogout: handleLogout
  })

  return (
    <div className="min-h-screen">
      {!isAuthenticated ? (
        <LoginPage onLogin={handleLogin} />
      ) : (
        <DataForgeApp onLogout={handleLogout} />
      )}
    </div>
  )
}

export default App
