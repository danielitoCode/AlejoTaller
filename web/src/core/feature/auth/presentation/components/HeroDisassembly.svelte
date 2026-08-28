<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import gsap from "gsap";

    type PieceDef = {
        id: string;
        src: string;
        z: number;
        /** assembled left/top as % of stage; width as % of stage */
        ax: number;
        ay: number;
        aw: number;
        /** exploded delta (px) + rotation (deg) */
        dx: number;
        dy: number;
        rot: number;
        delay: number;
    };

    /**
     * Layout marketing (no coords en el zip original).
     * Ajustable: ax/ay/aw = posición armada; dx/dy/rot = despiece.
     */
    const PIECES: PieceDef[] = [
        { id: "01", src: "/hero-disassembly/01_tapa_superior.webp", z: 20, ax: 28, ay: 2, aw: 44, dx: 0, dy: -72, rot: -6, delay: 0 },
        { id: "02", src: "/hero-disassembly/02_carcasa_superior.webp", z: 18, ax: 26, ay: 8, aw: 48, dx: 12, dy: -50, rot: 4, delay: 0.04 },
        { id: "03", src: "/hero-disassembly/03_marco_soporte.webp", z: 16, ax: 25, ay: 14, aw: 50, dx: -10, dy: -30, rot: -3, delay: 0.08 },
        { id: "04", src: "/hero-disassembly/04_modulo_ventilacion_superior.webp", z: 15, ax: 28, ay: 12, aw: 44, dx: 42, dy: -58, rot: 8, delay: 0.1 },
        { id: "05", src: "/hero-disassembly/05_ventiladores_internos.webp", z: 14, ax: 27, ay: 16, aw: 46, dx: -38, dy: -42, rot: -10, delay: 0.12 },
        { id: "06", src: "/hero-disassembly/06_disipadores.webp", z: 12, ax: 18, ay: 20, aw: 64, dx: 58, dy: -22, rot: 12, delay: 0.14 },
        { id: "07", src: "/hero-disassembly/07_pcb_principal.webp", z: 11, ax: 27, ay: 24, aw: 46, dx: -52, dy: 6, rot: -8, delay: 0.16 },
        { id: "08", src: "/hero-disassembly/08_modulos_electronicos.webp", z: 10, ax: 18, ay: 22, aw: 64, dx: 62, dy: 16, rot: 6, delay: 0.18 },
        { id: "09", src: "/hero-disassembly/09_bateria_modulos_celdas.webp", z: 9, ax: 17, ay: 26, aw: 66, dx: -22, dy: 32, rot: 3, delay: 0.2 },
        { id: "10", src: "/hero-disassembly/10_estructura_interna.webp", z: 8, ax: 26, ay: 28, aw: 48, dx: 16, dy: 42, rot: -4, delay: 0.22 },
        { id: "11", src: "/hero-disassembly/11_panel_frontal.webp", z: 13, ax: 22, ay: 18, aw: 56, dx: 0, dy: 58, rot: 2, delay: 0.15 },
        { id: "12", src: "/hero-disassembly/12_pantalla_display.webp", z: 17, ax: 18, ay: 22, aw: 64, dx: 6, dy: -18, rot: -2, delay: 0.11 },
        { id: "13", src: "/hero-disassembly/13_panel_conectores.webp", z: 12, ax: 24, ay: 20, aw: 52, dx: -68, dy: 22, rot: -12, delay: 0.17 },
        { id: "14", src: "/hero-disassembly/14_panel_lateral_izquierdo.webp", z: 7, ax: 12, ay: 18, aw: 42, dx: -84, dy: 12, rot: -18, delay: 0.19 },
        { id: "15", src: "/hero-disassembly/15_panel_lateral_derecho.webp", z: 7, ax: 46, ay: 18, aw: 42, dx: 84, dy: 12, rot: 18, delay: 0.19 },
        { id: "16", src: "/hero-disassembly/16_base_inferior.webp", z: 5, ax: 24, ay: 58, aw: 52, dx: 0, dy: 72, rot: 2, delay: 0.24 },
        { id: "17", src: "/hero-disassembly/17_ruedas.webp", z: 4, ax: 26, ay: 48, aw: 48, dx: -42, dy: 88, rot: -5, delay: 0.26 },
        { id: "18", src: "/hero-disassembly/18_asa_riel_inferior.webp", z: 6, ax: 24, ay: 62, aw: 52, dx: 32, dy: 78, rot: 4, delay: 0.25 },
        { id: "19", src: "/hero-disassembly/19_soportes_internos.webp", z: 3, ax: 25, ay: 55, aw: 50, dx: -58, dy: 62, rot: -8, delay: 0.23 },
        { id: "20", src: "/hero-disassembly/20_tornilleria_soportes.webp", z: 19, ax: 35, ay: 35, aw: 30, dx: 92, dy: -32, rot: 25, delay: 0.28 }
    ];

    let plateEl: HTMLDivElement | null = null;
    let tl: gsap.core.Timeline | null = null;

    onMount(() => {
        if (typeof window === "undefined" || !plateEl) return;

        const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        const nodes = Array.from(plateEl.querySelectorAll<HTMLElement>(".hd-piece"));
        if (!nodes.length) return;

        if (reduced) {
            nodes.forEach((el) => gsap.set(el, { x: 0, y: 0, rotation: 0, opacity: 1 }));
            return;
        }

        nodes.forEach((el, i) => {
            const p = PIECES[i];
            gsap.set(el, {
                x: p.dx,
                y: p.dy,
                rotation: p.rot,
                opacity: 0.88,
                transformOrigin: "50% 50%"
            });
        });

        tl = gsap.timeline({ repeat: -1, defaults: { ease: "power2.inOut" } });

        nodes.forEach((el, i) => {
            const p = PIECES[i];
            tl!.to(
                el,
                { x: 0, y: 0, rotation: 0, opacity: 1, duration: 1.15, delay: p.delay },
                0
            );
        });

        tl.to({}, { duration: 1.7 });

        nodes.forEach((el, i) => {
            const p = PIECES[i];
            tl!.to(
                el,
                {
                    x: p.dx,
                    y: p.dy,
                    rotation: p.rot,
                    opacity: 0.88,
                    duration: 1.25,
                    delay: p.delay * 0.55
                },
                "explode"
            );
        });

        tl.to({}, { duration: 0.95 });
    });

    onDestroy(() => {
        tl?.kill();
        tl = null;
    });
</script>

<div class="hd-stage" role="img" aria-label="Despiece animado de estación de energía">
    <div class="hd-glow" aria-hidden="true"></div>
    <div class="hd-plate" bind:this={plateEl}>
        {#each PIECES as p}
            <img
                class="hd-piece"
                src={p.src}
                alt=""
                draggable="false"
                loading="eager"
                decoding="async"
                style="left:{p.ax}%;top:{p.ay}%;width:{p.aw}%;z-index:{p.z}"
            />
        {/each}
    </div>
</div>

<style>
    .hd-stage {
        position: relative;
        width: 100%;
        aspect-ratio: 560 / 420;
        max-height: min(320px, 36vh);
        margin: 0 auto;
    }
    .hd-glow {
        position: absolute;
        inset: 12% 6% -6%;
        background: radial-gradient(
            ellipse at 50% 70%,
            color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent),
            transparent 65%
        );
        filter: blur(28px);
        pointer-events: none;
    }
    .hd-plate {
        position: relative;
        width: 100%;
        height: 100%;
        border-radius: 20px;
        overflow: hidden;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
        background:
            radial-gradient(
                ellipse 60% 50% at 50% 45%,
                color-mix(in srgb, var(--md-sys-color-surface-container-high) 75%, transparent),
                var(--md-sys-color-surface-container-lowest, var(--md-sys-color-surface))
            );
        box-shadow: 0 28px 56px color-mix(in srgb, black 18%, transparent);
    }
    .hd-piece {
        position: absolute;
        height: auto;
        max-width: none;
        pointer-events: none;
        user-select: none;
        will-change: transform, opacity;
        filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.35));
    }
    @media (max-width: 899px) {
        .hd-stage {
            max-height: 180px;
        }
    }
</style>
