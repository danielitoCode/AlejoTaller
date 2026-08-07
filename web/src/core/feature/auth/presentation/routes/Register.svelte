<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { Button, Card, TextFieldOutlined } from "m3-svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import PersonRounded from "@ktibow/iconset-material-symbols/person-rounded";
    import MailOutlineRounded from "@ktibow/iconset-material-symbols/mail-outline-rounded";
    import LockOutline from "@ktibow/iconset-material-symbols/lock-outline";
    import VisibilityRounded from "@ktibow/iconset-material-symbols/visibility-rounded";
    import VisibilityOffRounded from "@ktibow/iconset-material-symbols/visibility-off-rounded";
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";
    import { registerStore } from "../viewmodel/register.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";

    export let navController: NavController;

    let name = "";
    let email = "";
    let password = "";
    let confirmPassword = "";
    let showPassword = false;
    let showConfirmPassword = false;
    let submitting = false;
    let localError: string | null = null;

    $: canSubmit =
        name.trim().length > 1 &&
        email.trim().length > 3 &&
        password.trim().length > 3 &&
        confirmPassword === password &&
        !$registerStore.loading &&
        !submitting;

    $: normalizedEmail = email.trim().toLowerCase();

    async function submit() {
        if (submitting) return;
        localError = null;

        if (password !== confirmPassword) {
            localError = "Las contrasenas no coinciden";
            return;
        }
        if (!canSubmit) return;

        try {
            submitting = true;
            await registerStore.createAccount({
                name: name.trim(),
                email: normalizedEmail,
                password,
                phone: "",
                photo_url: "",
                role: "viewer",
                verification: false
            });
            toastStore.success("Cuenta creada. Ya puedes iniciar sesion.");
            navController.navigate("login");
        } catch (e) {
            localError = e instanceof Error ? e.message : "No se pudo registrar";
            toastStore.error(localError);
        } finally {
            submitting = false;
        }
    }

    function goToLogin() {
        navController.navigate("login");
    }
</script>

<AuthBusyOverlay
    open={submitting || $registerStore.loading}
    title="Creando tu cuenta…"
    subtitle="Estamos registrando tus datos. No cierres esta ventana."
/>

<Screen ariaLabel="Registro" scrollable={false}>
    <main class="register-screen">
        <div class="register-shell">
            <section class="register-brand">
                <img class="register-logo" src="/alejoicon_clean.svg" alt="Logo" />
                <h2>Crear cuenta</h2>
                <p>Registrate para comprar y reservar en Taller Alejo</p>
            </section>

            <section class="register-card-wrap">
                <Card variant="filled">
                    <div class="register-card-content">
                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Nombre"
                                bind:value={name}
                                leadingIcon={PersonRounded}
                                enter={submit}
                            />
                        </div>

                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Correo"
                                bind:value={email}
                                type="email"
                                leadingIcon={MailOutlineRounded}
                                enter={submit}
                            />
                        </div>

                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Contrasena"
                                bind:value={password}
                                type={showPassword ? "text" : "password"}
                                leadingIcon={LockOutline}
                                trailing={{
                                    icon: showPassword ? VisibilityOffRounded : VisibilityRounded,
                                    onclick: () => {
                                        showPassword = !showPassword;
                                    },
                                    "aria-label": showPassword ? "Ocultar contrasena" : "Mostrar contrasena",
                                    title: showPassword ? "Ocultar contrasena" : "Mostrar contrasena"
                                }}
                                enter={submit}
                            />
                        </div>

                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Confirmar contrasena"
                                bind:value={confirmPassword}
                                type={showConfirmPassword ? "text" : "password"}
                                leadingIcon={LockOutline}
                                trailing={{
                                    icon: showConfirmPassword ? VisibilityOffRounded : VisibilityRounded,
                                    onclick: () => {
                                        showConfirmPassword = !showConfirmPassword;
                                    },
                                    "aria-label": showConfirmPassword ? "Ocultar contrasena" : "Mostrar contrasena",
                                    title: showConfirmPassword ? "Ocultar contrasena" : "Mostrar contrasena"
                                }}
                                enter={submit}
                            />
                        </div>

                        <div class="action-row">
                            <Button variant="filled" size="m" disabled={!canSubmit} onclick={submit}>
                                <span class="btn-content">Registrarse</span>
                            </Button>
                        </div>

                        <div class="action-row">
                            <Button variant="text" size="m" onclick={goToLogin}>
                                Ya tienes cuenta? Inicia sesion
                            </Button>
                        </div>

                        {#if localError || $registerStore.error}
                            <p class="error-copy">{localError ?? $registerStore.error}</p>
                        {/if}
                    </div>
                </Card>
            </section>
        </div>
    </main>
</Screen>

<style>
    :global(.screen[aria-label="Registro"]) {
        width: 100%;
        max-width: none;
        height: 100dvh;
        min-height: 100dvh;
        padding: 0;
        margin: 0;
    }

    .register-screen {
        width: 100%;
        height: 100%;
        max-height: 100%;
        padding:
            max(14px, calc(env(safe-area-inset-top) + 8px))
            16px
            max(16px, calc(env(safe-area-inset-bottom) + 10px));
        box-sizing: border-box;
        color: var(--md-sys-color-on-background);
        background:
            linear-gradient(
                color-mix(in srgb, var(--md-sys-color-primary-container) 16%, var(--md-sys-color-background)),
                color-mix(in srgb, var(--md-sys-color-background) 88%, var(--md-sys-color-surface-container-low))
            );
        overflow-x: hidden;
        overflow-y: auto;
    }

    .register-shell {
        width: 100%;
        max-width: 480px;
        margin: 0 auto;
        display: grid;
        gap: 18px;
        align-content: start;
    }

    .register-brand {
        display: grid;
        justify-items: center;
        text-align: center;
        gap: 8px;
    }

    .register-logo {
        width: 64px;
        height: 64px;
        object-fit: contain;
    }

    .register-brand h2 {
        margin: 0;
        font-size: 1.6rem;
        font-weight: 800;
    }

    .register-brand p {
        margin: 0;
        color: var(--md-sys-color-on-surface-variant);
    }

    .register-card-content {
        padding: 16px;
        display: grid;
        gap: 12px;
    }

    .field-wrap :global(.m3-container) {
        width: 100%;
        height: 56px;
    }

    .action-row :global(.m3-container) {
        width: 100%;
    }

    .btn-content {
        display: inline-flex;
        padding: 12px 0;
        font-weight: 600;
    }

    .error-copy {
        margin: 0;
        color: var(--md-sys-color-error);
        text-align: center;
        font-size: 0.9rem;
    }
</style>
