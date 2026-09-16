<script lang="ts">
    import { getAuthPort } from "../../di/authPort.factory";
    import { Button, TextFieldOutlined } from "m3-svelte";
    import MailOutlineRounded from "@ktibow/iconset-material-symbols/mail-outline-rounded";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logAuth0 } from "../../../../infrastructure/presentation/navigation/debug-logger";

    export let disabled = false;

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
            logAuth0("warn", "Login: IdP inactivo");
            toastStore.error("Auth no activo. Revisa VITE_CLERK_PUBLISHABLE_KEY.");
            return;
        }
        loading = true;
        try {
            logAuth0(
                "info",
                `Login UI connection=${opts?.connection ?? "hosted"} screen=${opts?.screenHint ?? "login"}`,
            );
            await auth.init();
            await auth.loginWithRedirect({
                returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                connection: opts?.connection,
                screenHint: opts?.screenHint,
                loginHint: opts?.loginHint,
            });
        } catch (e) {
            const msg = e instanceof Error ? e.message : "No se pudo iniciar sesión";
            logAuth0("error", `Login UI falló: ${msg}`, e);
            toastStore.error(msg);
            loading = false;
        }
    }

    function signInEmail() {
        void login({ screenHint: "login", loginHint: hint || undefined });
    }

    function signInGoogle() {
        void login({ connection: "google", screenHint: "login" });
    }

    function signUp() {
        void login({ screenHint: "signup", loginHint: hint || undefined });
    }
</script>

<div class="auth0-block">
    <div class="field-wrap">
        <TextFieldOutlined
            label="Correo (opcional)"
            bind:value={email}
            type="email"
            leadingIcon={MailOutlineRounded}
            enter={signInEmail}
        />
    </div>

    <Button variant="filled" size="m" disabled={disabled || loading} onclick={signInEmail}>
        {#if loading}Redirigiendo…{:else}Entrar con email / contraseña{/if}
    </Button>

    <Button variant="outlined" size="m" disabled={disabled || loading} onclick={signInGoogle}>
        Continuar con Google
    </Button>

    <Button variant="text" size="m" disabled={disabled || loading} onclick={signUp}>
        Crear cuenta nueva (email)
    </Button>

    <p class="hint">
        Acceso vía <strong>Clerk</strong> (sin Appwrite ni Auth0). Google y email se configuran en el
        dashboard de Clerk. Roles: <code>publicMetadata.role</code>.
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
