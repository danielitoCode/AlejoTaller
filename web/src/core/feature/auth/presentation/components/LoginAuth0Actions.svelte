<script lang="ts">
    import { getAuthPort } from "../../di/authPort.factory";
    import { Button, TextFieldOutlined } from "m3-svelte";
    import MailOutlineRounded from "@ktibow/iconset-material-symbols/mail-outline-rounded";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logAuth0 } from "../../../../infrastructure/presentation/navigation/debug-logger";

    export let disabled = false;

    /** Auth0 Database connection (default tenant name). */
    const DB_CONNECTION = "Username-Password-Authentication";

    let email = "";
    let loading = false;

    $: hint = email.trim().toLowerCase();

    async function login(opts?: {
        connection?: string;
        screenHint?: "signup" | "login";
        loginHint?: string;
    }) {
        const auth = getAuthPort();
        if (!auth) {
            logAuth0("warn", "Login: Auth0 inactivo");
            toastStore.error("Auth0 no está activo. Revisa VITE_AUTH0_DOMAIN / CLIENT_ID.");
            return;
        }
        loading = true;
        try {
            logAuth0(
                "info",
                `Login UI connection=${opts?.connection ?? "universal"} screen=${opts?.screenHint ?? "login"}`,
            );
            await auth.init();
            await auth.loginWithRedirect({
                returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                connection: opts?.connection,
                screenHint: opts?.screenHint,
                loginHint: opts?.loginHint,
            });
            // redirect: no vuelve aquí
        } catch (e) {
            const msg = e instanceof Error ? e.message : "No se pudo iniciar Auth0";
            logAuth0("error", `Login UI falló: ${msg}`, e);
            toastStore.error(msg);
            loading = false;
        }
    }

    /** Email → Auth0 Universal Login (Database). Google → connection social. */
    function signInEmail() {
        void login({
            connection: DB_CONNECTION,
            screenHint: "login",
            loginHint: hint || undefined,
        });
    }

    function signInGoogle() {
        void login({ connection: "google-oauth2", screenHint: "login" });
    }

    function signUp() {
        void login({
            connection: DB_CONNECTION,
            screenHint: "signup",
            loginHint: hint || undefined,
        });
    }
</script>

<div class="auth0-block">
    <div class="field-wrap">
        <TextFieldOutlined
            label="Correo (opcional, pre-rellena Auth0)"
            bind:value={email}
            type="email"
            leadingIcon={MailOutlineRounded}
            enter={signInEmail}
        />
    </div>

    <Button variant="filled" size="m" disabled={disabled || loading} onclick={signInEmail}>
        {#if loading}Redirigiendo a Auth0…{:else}Entrar con email / contraseña{/if}
    </Button>

    <Button variant="outlined" size="m" disabled={disabled || loading} onclick={signInGoogle}>
        Continuar con Google
    </Button>

    <Button variant="text" size="m" disabled={disabled || loading} onclick={signUp}>
        Crear cuenta nueva (email)
    </Button>

    <p class="hint">
        Todo el acceso es <strong>Auth0</strong> (no Appwrite). Si ya entraste con Google antes, usa
        <em>Continuar con Google</em> — Auth0 reconoce la misma cuenta. Una cuenta en Appwrite
        <strong>no</strong> cuenta como cuenta Auth0.
    </p>
</div>

<style>
    .auth0-block {
        display: grid;
        gap: 10px;
        width: 100%;
    }
    .field-wrap {
        display: grid;
    }
    .hint {
        margin: 0;
        text-align: center;
        font-size: 0.78rem;
        line-height: 1.35;
        color: var(--md-sys-color-on-surface-variant);
    }
</style>
