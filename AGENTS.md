# Noita Util

## Project Identity

```text
ID: com.jalbarrang.noita-util
Repository: https://github.com/jalbarrang/noita-util
```

## Package Management

- Prefer using `bun` for package management, avoid using `npm`/`yarn`/`pnpm`.
- Prefer using the available `package.json` scripts instead of running commands directly for typechecking, linting, formatting, testing, etc.

## Code Style

- Never create barrel files, always use named exports.

## React Patterns

- This project uses Base UI patterns, not older Radix-style composition.
- Do not assume local wrapper components support `asChild`.
- Before using common shadcn or Radix idioms, check the local wrapper API first.
- Prefer the repo's existing `render={...}` composition patterns when working with triggers and buttons.
- Destructure props inside the component body, not in the function signature.
- use `type` instead of `interface` for component props.
- Don't overuse `useEffect` for simple state updates.
- Don't use deprecated `forwardRef` for component refs, pass the `ref` as a prop.
- This project should follow the React 19+ composition patterns.
- This project doesn't use React Server Components.
- Never modify the generated `routeTree.gen.ts` file, if there are type errors related to the routes, use the `generate-routes` script to fix them.

## Task Management

Use `dex` cli to break down complex work, track progress across sessions, and coordinate multi-step implementations.
