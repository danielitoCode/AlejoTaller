<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { Button, Card, TextFieldOutlined } from "m3-svelte";
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
    let localError: string | null = null;
    let submitting = false;

    $: passwordsMatch = password.length > 0 && password === confirmPassword;
    $: canSubmit =
        name.trim().length > 2 &&
        email.trim().length > 4 &&
        password.trim().length > 5 &&
        confirmPassword.trim().length > 5 &&
        passwordsMatch &&
        !$registerStore.loading &&
        !submitting;

    $: normalizedEmail = email.trim().toLowerCase();

    async function submit() {
        if (submitting) return;
        localError = null;

        if (!passwordsMatch) {
            localError = "Las contrasenas no coinciden.";
            toastStore.error(localError);
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
                sub: "",
                verification: false
            });
            toastStore.success("Cuenta creada correctamente. Inicia sesion para continuar.");
            navController.resetTo("login");
        } catch (error) {
            localError = error instanceof Error ? error.message : "No se pudo crear la cuenta.";
            toastStore.error(localError);
        } finally {
            submitting = false;
        }
    }

    function goToLogin() {
        navController.navigate("login");
    }
</script>

<Screen ariaLabel="Registro" scrollable={false}>
    <main class="register-screen">
        <div class="register-shell">
            <section class="register-header">
                <div class="register-copy">
                    <h1>Crear cuenta</h1>
                    <p>Completa tus datos para comenzar.</p>
                </div>
            </section>

            <section class="register-form-wrap">
                <Card variant="filled">
                    <div class="register-form">
                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Nombre completo"
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
                                    "aria-label": showConfirmPassword ? "Ocultar confirmacion" : "Mostrar confirmacion",
                                    title: showConfirmPassword ? "Ocultar confirmacion" : "Mostrar confirmacion"
                                }}
                                error={confirmPassword.length > 0 && !passwordsMatch}
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
            ),
            url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180' viewBox='0 0 180 180'%3E%3Cg fill='none' stroke='%232e4f5a' stroke-width='3' stroke-linecap='round' opacity='0.42'%3E%3Cpath d='M12 20h40v28h18v-18h34v44h24v-20h40'/%3E%3Cpath d='M18 92h28v42h36v-24h30v44h56'/%3E%3Cpath d='M30 64v54'/%3E%3Cpath d='M90 14v34'/%3E%3Cpath d='M136 74v60'/%3E%3Cpath d='M64 134v30h34'/%3E%3C/g%3E%3Cg fill='%232b4953' opacity='0.38'%3E%3Crect x='22' y='22' width='8' height='8' rx='4'/%3E%3Crect x='86' y='54' width='8' height='8' rx='4'/%3E%3Crect x='144' y='38' width='8' height='8' rx='4'/%3E%3Crect x='56' y='112' width='8' height='8' rx='4'/%3E%3Crect x='118' y='142' width='8' height='8' rx='4'/%3E%3C/g%3E%3C/svg%3E");
        background-size: cover, 180px 180px;
        background-position: center, center;
        overflow-x: hidden;
        overflow-y: hidden;
    }

    .register-shell {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        gap: clamp(14px, 2vh, 22px);
    }

    .register-header {
        display: grid;
        gap: 12px;
    }

    .register-copy {
        display: grid;
        justify-items: center;
        text-align: center;
        gap: 8px;
    }

    .register-copy h1 {
        margin: 0;
        font-size: clamp(2rem, 7vw, 2.5rem);
        line-height: 1.05;
        font-weight: 500;
    }

    .register-copy p {
        margin: 0;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 1rem;
    }

    .register-form-wrap {
        width: 100%;
        display: grid;
        justify-items: center;
    }

    .register-form-wrap :global(.m3-container.filled) {
        width: min(100%, 560px);
        border-radius: 20px;
        background: var(--md-sys-color-surface-container-high);
        --m3v-background: var(--md-sys-color-surface-container-high);
        box-shadow: 0 16px 36px rgb(0 0 0 / 0.22);
    }

    .register-form {
        width: 100%;
        padding: 14px;
        display: grid;
        gap: 10px;
    }

    .field-wrap {
        display: grid;
    }

    .field-wrap :global(.m3-container) {
        width: 100%;
        min-width: 0;
        height: 56px;
        --m3-field-outlined-shape: 0.85rem;
        --m3v-background: var(--md-sys-color-surface-container);
    }

    .field-wrap :global(input) {
        min-height: 56px;
        color: var(--md-sys-color-on-surface);
    }

    .field-wrap :global(label) {
        color: var(--md-sys-color-on-surface-variant);
    }

    .field-wrap :global(.layer) {
        border-color: var(--md-sys-color-outline);
        border-width: 1.5px;
    }

    .field-wrap :global(.leading),
    .field-wrap :global(.trailing) {
        color: var(--md-sys-color-on-surface-variant);
    }

    .field-wrap :global(input:focus ~ .layer) {
        border-color: var(--md-sys-color-primary);
    }

    .field-wrap :global(input:focus ~ label) {
        color: var(--md-sys-color-primary);
    }

    .action-row :global(.m3-container) {
        width: 100%;
        min-width: 0;
        max-width: 100%;
    }

    .action-row :global(.m3-container.m) {
        padding-inline: clamp(1.2rem, 4vw, 1.85rem);
    }

    .btn-content {
        width: 100%;
        min-width: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        font-weight: 500;
        padding: 12px 0;
    }

    .error-copy {
        margin: 0;
        color: var(--md-sys-color-error);
        text-align: center;
        font-size: 0.9rem;
    }

    @media (max-height: 760px) {
        .register-screen {
            padding-top: max(10px, calc(env(safe-area-inset-top) + 4px));
            padding-bottom: max(12px, calc(env(safe-area-inset-bottom) + 6px));
        }

        .register-shell {
            gap: 12px;
        }

        .register-form {
            padding: 12px;
            gap: 8px;
        }

        .field-wrap :global(.m3-container) {
            height: 54px;
        }

        .field-wrap :global(input) {
            min-height: 54px;
        }
    }

    @media (min-width: 900px) {
        .register-screen {
            padding:
                max(18px, calc(env(safe-area-inset-top) + 10px))
                28px
                max(18px, calc(env(safe-area-inset-bottom) + 10px));
        }

        .register-shell {
            max-width: 1160px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: minmax(320px, 0.9fr) minmax(420px, 1fr);
            align-items: center;
            gap: clamp(24px, 4vw, 44px);
        }

        .register-header {
            align-content: center;
            min-height: 0;
        }

        .register-copy {
            justify-items: start;
            text-align: left;
        }

        .register-copy h1 {
            font-size: clamp(2.8rem, 4vw, 4rem);
        }

        .register-copy p {
            font-size: 1.08rem;
        }

        .register-form-wrap :global(.m3-container.filled) {
            width: min(100%, 620px);
        }
    }
</style>
