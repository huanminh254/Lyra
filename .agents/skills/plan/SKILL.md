---
name: "plan"
description: "Run the complete Spec Kit discovery and planning workflow for a new project or feature."
metadata:
  author: "Sound project"
---

# One-command project planning

Use this skill when the user invokes `$plan` followed by a project or feature idea.

The argument after `$plan` is the initial product idea. Preserve it and run these
stages in order:

1. `$speckit-specify <the user's idea>`
2. `$speckit-clarify`
3. `$speckit-plan`
4. `$speckit-tasks`

## Interaction rules

- Do not implement or modify application source code. This skill ends after
  `tasks.md` is generated.
- The specify stage creates the feature specification and may ask critical
  questions. Stop and wait for the user's answers whenever that stage requires
  input.
- After specification is created, run clarify. It asks one targeted question at
  a time and updates the spec after each accepted answer. Stop and wait after
  each question; when the user signals `done`, continue with the answers already
  recorded.
- Only start plan after clarification has finished or the user explicitly asks
  to proceed. Only start tasks after plan has completed successfully.
- If a stage fails because its required artifact is missing, report the exact
  missing artifact and stop; do not invent a replacement.
- Pass the original `$plan` arguments to specify as the feature description.
  If the arguments are empty, ask the user for a short idea before starting.

## Completion

Report the generated feature directory and links/paths to `spec.md`,
`plan.md`, design artifacts, and `tasks.md`, plus a brief summary of the task
count and suggested MVP scope. Do not invoke `$speckit-implement` automatically.
