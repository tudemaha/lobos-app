# AGENTS.md

This file provides guidance for AI agents working on this codebase.

---

## Project Overview

A Spring Boot REST API with a Thymeleaf web interface.
- **Backend**: Spring Boot, JDBC API (raw SQL via JdbcTemplate), MySQL
- **Frontend**: Thymeleaf templating engine, Tailwind CSS
- **Auth**: Spring Security (session-based for Thymeleaf UI)
- **IDs**: UUID (auto-generated in MySQL, stored as VARCHAR(36))
- **Packaging**: JAR (embedded Tomcat)
- **Java version**: 21

---

## Frontend Rules

### Design System
- **Main design**: use a clean and minimalist design for each page.
- **Font**: Garamond
- **Color**: monochrome with pastel blue `#70A3C9`.
- **Responsibility**: each page have to be responsive, with mobile view optimized.

### Technology
- **Templating**: Thymeleaf. All templates in `src/main/resources/templates/`.
- **CSS**: Tailwind CSS via CDN (`https://cdn.tailwindcss.com`). No build step required.
- **No JavaScript frameworks** — vanilla JS only if needed (e.g. confirm dialogs, dynamic UI).
### Tailwind setup
Include this in the `<head>` of every template (or in `layout.html`):
```html
<script src="https://cdn.tailwindcss.com"></script>
```

Do not use arbitrary Tailwind values like `w-[347px]` unless there is no standard utility
that fits — prefer standard scale values (`w-80`, `p-4`, `text-sm`, etc.).

### Layout / base template
- Use `layout.html` as the shared base. All pages extend it using Thymeleaf fragments:
- Individual pages replace the `<main>` block with their own content using `th:replace` or `th:insert`.

### Thymeleaf conventions
- Always declare namespaces on the `<html>` tag:
```html
  xmlns:th="http://www.thymeleaf.org"
  xmlns:sec="http://www.thymeleaf.org/extras/spring-security"
```
- Use `th:text` to render values — never interpolate directly with `${...}` inside raw HTML text nodes.
- Use `th:href="@{/path/{id}(id=${item.id})}"` for dynamic URLs — never concatenate strings.
- Use `th:object` and `th:field` for form binding. Always pair with `BindingResult` in the controller.
- Use `th:each` for loops, `th:if` / `th:unless` for conditionals.
- Use `sec:authorize="isAuthenticated()"` / `sec:authorize="hasRole('ADMIN')"` to show/hide elements.

### CSRF
Every `<form method="post">` must include the CSRF token:
```html
<input type="hidden"
       th:name="${_csrf.parameterName}"
       th:value="${_csrf.token}"/>
```
Missing CSRF tokens will cause `403 Forbidden` on form submissions.

### Forms and validation errors
Always show field-level validation errors next to the field:
```html
<input type="text" th:field="*{title}"
       class="..."/>
<span th:if="${#fields.hasErrors('title')}"
      th:errors="*{title}"
      class="...">
</span>
```
Show a summary at the top of the form for global errors:
```html
<div th:if="${#fields.hasErrors('*')}"
     class="...">
    <ul class="list-disc list-inside">
        <li th:each="error : ${#fields.allErrors()}" th:text="${error}"></li>
    </ul>
</div>
```