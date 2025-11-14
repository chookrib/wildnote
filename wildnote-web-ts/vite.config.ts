import { fileURLToPath, URL } from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

import Components from 'unplugin-vue-components/vite';
import {AntDesignVueResolver} from 'unplugin-vue-components/resolvers';

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        Components({
            dts: 'src/components.d.ts',
            resolvers: [
                AntDesignVueResolver({
                    importStyle: false, // css in js
                }),
            ],
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    build: {
        rollupOptions: {
            input: {
                index: fileURLToPath(new URL('./index.html', import.meta.url)),
                login: fileURLToPath(new URL('./login.html', import.meta.url)),
            }
        }
    },
})
