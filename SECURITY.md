# Security Policy

## Supported versions

This repository is a learning / demo project. Security fixes are applied on the latest `main` branch only.

## Reporting a vulnerability

Please **do not** open a public GitHub issue for security-sensitive reports.

Instead, use [GitHub Security Advisories](https://github.com/SrinathRayabarapu/agent-integration-demo/security/advisories/new) for this repository, or email the maintainer via the contact details on their [GitHub profile](https://github.com/SrinathRayabarapu).

Include:

- A description of the issue and impact
- Steps to reproduce
- Any suggested remediation

## Secrets and credentials

- Never commit AWS Bedrock tokens, API keys, private keys, or `.env` files.
- Prefer environment variables (for example `AWS_BEARER_TOKEN_BEDROCK`).
- Rotate any credential that may have been exposed.

## Demo data

Sample account numbers and balances under `com.srinath.internal.AccountData` are synthetic fixtures for local demos only.
