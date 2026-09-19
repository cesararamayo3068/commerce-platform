# Commerce Platform — Frontend

SPA Angular del proyecto Commerce Platform. Consume la API REST del backend (`http://localhost:18080/api`).

## Stack

- Angular 21 (standalone components)
- TypeScript estricto
- Angular Router, HttpClient, Reactive Forms
- Signals (estado del carrito)
- SCSS propio (sin librerías de UI externas)
- Vitest (stack de testing oficial de Angular)

## Configuración

Toda la configuración está centralizada en `src/app/core/config/environment.ts`:

- `apiUrl`: base de la API backend (`http://localhost:18080/api`)
- `demoUserId`: usuario temporal (`1`) hasta que exista autenticación JWT

## Desarrollo

```bash
npm ci
npm start        # dev server en http://localhost:4200
```

## Tests y build

```bash
npm test         # Vitest
npm run build    # production build en dist/
```

## Estructura

```
src/app/
├── core/          # configuración, modelos, servicios, utilidades
├── shared/        # navbar, footer, toasts, product card, loading, empty state
└── features/      # products (catálogo, administración) y cart
```
