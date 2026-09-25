import { type CSSProperties, type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import dollarBill from "../../assets/dollarbillpixelart.png";
import { errorMessage } from "../../shared/api/api-error";
import { useLogin, useRegister } from "./session";

interface AuthPageProps {
  mode: "login" | "register";
}

const rainColumns = Array.from({ length: 8 }, (_, index) => index);
const billsPerColumn = 4;

function FallingBills() {
  return (
    <div className="money-rain" aria-hidden="true">
      {rainColumns.map((column) => (
        <div className="money-rain-column" key={column}>
          {Array.from({ length: billsPerColumn }, (_, row) => (
            <span
              className="falling-bill"
              key={row}
              style={{ "--fall-delay": `${-(row * 3 + (column % 2) * 1.5)}s` } as CSSProperties}
            >
              <img src={dollarBill} alt="" />
            </span>
          ))}
        </div>
      ))}
    </div>
  );
}

function AuthPage({ mode }: AuthPageProps) {
  const navigate = useNavigate();
  const login = useLogin();
  const register = useRegister();
  const mutation = mode === "login" ? login : register;
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");

  const isLogin = mode === "login";
  const title = isLogin ? "Welcome back" : "Create your workspace";
  const subtitle = isLogin
    ? "Sign in to manage your financial contexts."
    : "Create an account to start tracking your finances.";

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!name.trim() || !password) return;

    try {
      await mutation.mutateAsync({ name: name.trim(), password });
      navigate("/contexts", { replace: true });
    } catch {
      // Mutation state provides the server error.
    }
  }

  return (
    <main className="auth-page">
      <FallingBills />
      <section className="auth-card">
        <div className="brand auth-brand"><span className="brand-mark" aria-hidden="true">F</span>Financial App</div>
        <p className="eyebrow">Your financial workspace</p>
        <h1>{title}</h1>
        <p className="muted">{subtitle}</p>
        <form className="stack-form" onSubmit={submit}>
          <label>
            Name
            <input autoComplete="username" value={name} onChange={(event) => setName(event.target.value)} required />
          </label>
          <label>
            Password
            <input type="password" autoComplete={isLogin ? "current-password" : "new-password"} value={password} onChange={(event) => setPassword(event.target.value)} required />
          </label>
          {mutation.isError && <p className="form-error" role="alert">{errorMessage(mutation.error)}</p>}
          <button className="button button-primary" type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? "Please wait…" : isLogin ? "Sign in" : "Create account"}
          </button>
        </form>
        <p className="auth-switch">
          {isLogin ? "New here?" : "Already have an account?"} {" "}
          <Link to={isLogin ? "/register" : "/login"}>{isLogin ? "Create an account" : "Sign in"}</Link>
        </p>
      </section>
    </main>
  );
}

export function LoginPage() {
  return <AuthPage mode="login" />;
}

export function RegisterPage() {
  return <AuthPage mode="register" />;
}
