import { useState } from 'react'
import { helloWorldFromBackend } from './api/testClient'

function App() {
  const [message, setMessage] = useState('')

  async function handleClick() {
    try {
      const data = await helloWorldFromBackend()
      setMessage(data.message)
    } catch (err) {
      setMessage('Greška: ' + err.message)
    }
  }

  return (
    <div>
      <button onClick={handleClick}>Pozovi backend</button>
      <p>{message}</p>
    </div>
  )
}

export default App