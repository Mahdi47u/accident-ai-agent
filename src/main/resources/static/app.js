const form = document.querySelector("#chat-form");
const input = document.querySelector("#message");
const messages = document.querySelector("#messages");
const send = document.querySelector("#send");
const seed = document.querySelector("#seed");

function addMessage(role, text, error = false) {
  const article = document.createElement("article");
  article.className = `message ${role}${error ? " error" : ""}`;
  const author = document.createElement("b");
  author.textContent = role === "user" ? "You" : "Assistant";
  const body = document.createElement("p");
  body.textContent = text;
  article.append(author, body);
  messages.append(article);
  article.scrollIntoView({ behavior: "smooth", block: "end" });
}

async function sendMessage(text) {
  addMessage("user", text);
  send.disabled = true;
  send.textContent = "Thinking…";
  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: text }),
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "The request failed.");
    addMessage("assistant", data.answer);
  } catch (error) {
    addMessage("assistant", error.message, true);
  } finally {
    send.disabled = false;
    send.textContent = "Send";
    input.focus();
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const text = input.value.trim();
  if (!text) return;
  input.value = "";
  await sendMessage(text);
});

document.querySelectorAll(".suggestions button").forEach((button) => {
  button.addEventListener("click", () => {
    input.value = button.textContent;
    input.focus();
  });
});

seed.addEventListener("click", async () => {
  seed.disabled = true;
  seed.textContent = "Indexing…";
  try {
    const response = await fetch("/api/knowledge/seed", { method: "POST" });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Indexing failed.");
    addMessage("assistant", data.message);
  } catch (error) {
    addMessage("assistant", `${error.message} Check your OpenRouter key.`, true);
  } finally {
    seed.disabled = false;
    seed.textContent = "Index guidance";
  }
});

