<script lang="ts">
    import {Icon} from "m3-svelte";
    import SearchRounded from "@ktibow/iconset-material-symbols/search-rounded";
    import CloseRounded from "@ktibow/iconset-material-symbols/close-rounded";

    export let value = "";

    export let label = "";

    export let type:
        | "text"
        | "search"
        | "email"
        | "password"
        | "number" = "text";

    export let trailing:
        | {
        icon?: any;
        onclick?: () => void;
        ["aria-label"]?: string;
        title?: string;
    }
        | undefined = undefined;

    export let style = "";
    export let className = "";

    export let oninput: () => void = () => {};

    function handleInput() {
        oninput();
    }
</script>

<div
        class={`custom-textfield ${className}`}
        style={style}
>
    <input
            bind:value
            {type}
            placeholder={label}
            on:input={handleInput}
    />

    {#if trailing}
        <button
                class="trailing-button"
                type="button"
                aria-label={trailing["aria-label"]}
                title={trailing.title}
                on:click={trailing.onclick}
        >
            {#if trailing.icon}
                <Icon icon={trailing.icon} />
            {/if}
        </button>
    {/if}
</div>

<style>
    .custom-textfield {

        position: relative;
        width: 100%;

        /* IMPORTANTE */
        max-width: 720px;

        margin-inline: auto;

        height: 60px;

        display: flex;
        align-items: center;

        gap: 12px;

        padding: 0 18px;

        border-radius: 20px;

        background:
                linear-gradient(var(--md-sys-color-surface-container-high),
                var(--md-sys-color-surface-container-high))
                padding-box,
                linear-gradient(
                        135deg,
                        color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent),
                        color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent)
                )
                border-box;

        border: 1px solid transparent;

        box-shadow:
                0 1px 2px rgba(0, 0, 0, 0.04),
                0 4px 10px rgba(0, 0, 0, 0.06);

        transition:
                border-color .25s ease,
                box-shadow .25s ease,
                transform .25s ease,
                background .25s ease;
    }

    .custom-textfield:hover {
        background: var(--md-sys-color-surface-container);

        box-shadow:
                0 4px 14px rgba(0, 0, 0, 0.08),
                0 10px 24px rgba(0, 0, 0, 0.08);
    }

    .custom-textfield:focus-within {
        border-color: var(--md-sys-color-primary);

        background: var(--md-sys-color-surface);

        box-shadow:
                0 0 0 4px
                color-mix(
                        in srgb,
                        var(--md-sys-color-primary) 12%,
                        transparent
                ),
                0 12px 32px rgba(0, 0, 0, 0.12);

        transform: translateY(-2px);
    }

    .custom-textfield input {
        flex: 1;

        min-width: 0;

        border: none;
        outline: none;

        background: transparent;

        color: var(--md-sys-color-on-surface);

        font-size: 1rem;
        font-weight: 500;

        line-height: 1.4;
    }

    .custom-textfield input::placeholder {
        color: var(--md-sys-color-on-surface-variant);

        font-size: .92rem;

        font-weight: 400;

        transition:
                opacity .2s ease,
                transform .2s ease;
    }

    .custom-textfield:focus-within input::placeholder {
        opacity: .65;
    }

    .custom-textfield input::-webkit-search-cancel-button {
        appearance: none;
        -webkit-appearance: none;
        display: none;
    }

    .custom-textfield input::-webkit-search-decoration,
    .custom-textfield input::-webkit-search-results-button,
    .custom-textfield input::-webkit-search-results-decoration {
        appearance: none;
        -webkit-appearance: none;
        display: none;
    }

    .trailing-button {
        width: 40px;
        height: 40px;

        flex-shrink: 0;

        border: none;
        border-radius: 999px;

        background: transparent;

        color: var(--md-sys-color-on-surface-variant);

        display: flex;
        align-items: center;
        justify-content: center;

        cursor: pointer;

        transition:
                background .2s ease,
                color .2s ease,
                transform .2s ease;
    }

    .trailing-button:hover {
        background:
                color-mix(
                        in srgb,
                        var(--md-sys-color-primary) 10%,
                        transparent
                );

        color: var(--md-sys-color-primary);

        transform: scale(1.05);
    }

    .trailing-button:active {
        transform: scale(.94);
    }

    .trailing-button :global(svg) {
        width: 22px;
        height: 22px;
    }

    @media (min-width: 1200px) {
        .custom-textfield {
            max-width: 640px;
        }
    }

    @media (min-width: 768px) and (max-width: 1199px) {
        .custom-textfield {
            max-width: 580px;
        }
    }

    @media (max-width: 767px) {
        .custom-textfield {
            max-width: none;

            width: 100%;

            height: 56px;

            border-radius: 18px;
        }
    }
</style>