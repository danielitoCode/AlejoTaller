<script lang="ts">
    import { TextFieldOutlined, Icon } from "m3-svelte";
    import SearchRounded from "@ktibow/iconset-material-symbols/search-rounded";
    import CloseRounded from "@ktibow/iconset-material-symbols/close-rounded";
    import CustomTextField from "../../../../infrastructure/presentation/components/CustomTextField.svelte";

    let {
        query = $bindable(""),
        placeholder = "Buscar productos...",
        onQueryChanged = (value: string) => {},
        onClearQuery = () => {}
    }: {
        query?: string;
        placeholder?: string;
        onQueryChanged?: (value: string) => void;
        onClearQuery?: () => void;
    } = $props();
    $effect(() => {
        onQueryChanged(query);
    });
</script>

<div class="search-bar">
    <CustomTextField
            bind:value={query}
            label={placeholder}
            type="search"
            oninput={() => onQueryChanged(query)}
            trailing={{
        icon: query === "" ? SearchRounded : CloseRounded,
        onclick: () => {
            if (query !== "") {
                query = "";
                onClearQuery();
            }
        },
        "aria-label": query === "" ? "Buscar" : "Borrar",
        title: query === "" ? "Buscar" : "Borrar"
    }}
    />
</div>

<style>
    .search-bar {
        width: 100%;
    }

    .search-bar :global(.m3-textfield) {
        width: 100%;
    }

    .search-bar :global(.m3-textfield-wrapper) {
        width: 100%;
    }

    .search-bar :global(input) {
        width: 100%;
    }

    .search-input--custom :global(input::placeholder) {
        font-size: 0.8rem;
        color: var(--m3-sys-color-on-surface-variant);
        opacity: 0.7;
    }
</style>
