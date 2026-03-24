<script lang="ts">
    import { TextFieldOutlined, Icon } from "m3-svelte";
    import SearchRounded from "@ktibow/iconset-material-symbols/search-rounded";
    import CloseRounded from "@ktibow/iconset-material-symbols/close-rounded";

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
    <TextFieldOutlined
            label={placeholder}
            bind:value={query}
            type="search"
            leadingIcon={SearchRounded}
            class="search-input"
            oninput={() => onQueryChanged(query)}
            trailing={{
                                icon: query === "" ? SearchRounded : CloseRounded,
                                onclick: () => {
                                    if(query !== "") {
                                        query = "";
                                        onClearQuery()
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
</style>
