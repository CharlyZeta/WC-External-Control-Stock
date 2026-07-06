# Protocolo de Inicio de Sesión - StockDual (Orchestrator Mode)

> **ESTE ARCHIVO ES DE LECTURA OBLIGATORIA AL INICIAR LA SESIÓN.**

Como agente de Gemini CLI, para garantizar la máxima eficiencia y rigor técnico en StockDual, debes seguir este flujo de trabajo de orquestación inmediatamente:

1.  **Activación de Skill:** Invoca inmediatamente la skill `role-manager`.
2.  **Health Check & Context:** 
    - Verifica que Docker esté corriendo (`docker ps` para el contenedor de DB).
    - Lee el `ENGINEERING_CONTEXT.md`.
    - Presenta un breve estado de salud del proyecto al usuario.
3.  **Selección de Rol:** Pide al usuario que seleccione su función.
4.  **Misión:** Una vez cargado el rol, declara tu objetivo para la sesión basado en las tareas pendientes detectadas.

**No realices modificaciones de código hasta que el Health Check y la Misión estén definidos.**
