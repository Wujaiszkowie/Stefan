import { useState } from "react";

function App() {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState([]);

  const sendPrompt = async () => {
    const res = await fetch("http://localhost:8000/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text: input }),
    });
    const data = await res.json();
    setMessages([...messages, { user: input, bot: data.response }]);
    setInput("");
  };

  return (
    <div style={{ padding: 20 }}>
      <div>
        {messages.map((m, i) => (
          <div key={i} style={{ marginBottom: 10 }}>
            <b>You:</b> {m.user} <br />
            <b>Bot:</b> {m.bot}
          </div>
        ))}
      </div>
      <input
        value={input}
        onChange={e => setInput(e.target.value)}
        style={{ width: "300px", marginRight: "10px" }}
      />
      <button onClick={sendPrompt}>Send</button>
    </div>
  );
}

export default App;
