# Contrat de thread‑safety

## Résumé

- **Builders** (`Query`, `With/CTE`, `ConditionGroup`, futurs `Insert`/`Delete`) : **non thread‑safe**.  
  → *Un builder = un thread = une requête*. Ne jamais réutiliser/partager un builder entre threads.

- **Artefacts compilés** (`CompiledQuery`) : **thread‑safe et immuables**.  
  → On peut les mettre en cache et les partager sans risque.

- **Paramètres** (`SqlParameter`) : **thread‑safe et immuables**.  
  → Descripteurs (nom/type) **sans valeur**.

- **Bindings** (`SqlAndParams`) : **immuable et jetable**.  
  → Créé à chaque `bind(...)`, à utiliser pour l’exécution JDBC, puis laissé au GC.

## Contexte dialecte

Le dialecte SQL actif est stocké dans un `ThreadLocal` interne (`DialectContext`) **pendant la compilation/rendu**.  
C’est un détail d’implémentation : n’essayez pas de manipuler ce contexte via réflexion ou API internes.

- Choisissez le dialecte au moment de créer le builder (`SqlQuery.newQuery(Dialects.postgres())`, `schema.setDialect(...)`, etc.).
- Gardez un builder par thread et par dialecte. Dès qu’un builder est partagé entre threads, le `ThreadLocal` ne reflète plus la bonne valeur et la compilation devient indéterministe.

```java
Dialect postgres = Dialects.postgres(); // ou votre impl personnalisée
CompiledQuery cq = SqlQuery.newQuery(postgres)
    .select(EMP.ID, EMP.NAME)
    .from(EMP)
    .where(EMP.ID).eq(SqlParameter.of("id", Integer.class))
    .compile(); // le dialecte actif reste local à ce thread
```

> En environnement réactif/async, exécuter `render()` / `compile()` sur le thread qui a instancié la requête.  
> Sinon, créez un builder dédié par unité de travail et par thread pour éviter toute fuite de contexte.

## Cycle d’utilisation recommandé

1. **Construire** la requête via le DSL (builder éphémère, non partagé).  
2. **Compiler** → `CompiledQuery` (partageable, cache possible).  
3. **Binder** à l’exécution → `SqlAndParams` (nouvelle liste de valeurs à chaque appel).  
4. **Exécuter** via JDBC.

### Exemple : compiler une fois, binder à la volée

```java
// Au démarrage / cache applicatif
static final CompiledQuery FIND_EMP_BY_ID;
static {
  Dialect postgres = Dialects.postgres();
  FIND_EMP_BY_ID = SqlQuery.newQuery(postgres)
      .select(EMP.ID, EMP.NAME)
      .from(EMP)
      .where(EMP.ID).eq(SqlParameter.of("id", Integer.class))
      .compile();
}

// À chaque requête (multi‑thread)
var sap = FIND_EMP_BY_ID.bind(Map.of("id", someId));
jdbcTemplate.query(sap.sql(), rs -> { /* ... */ }, sap.params().toArray());
```

## Règles de sûreté

- Ne pas exposer/modifier des listes internes : les collections renvoyées par l’API sont immuables.  
- Éviter de passer des objets **mutables** comme littéraux fixes (ex. `java.util.Date`).  
  Utiliser des types immuables (`String`, `Integer`, `LocalDateTime`, …) ou des `SqlParameter`.  
- `RawSql` est prévu comme échappatoire : préférer des `SqlParameter` plutôt que concaténer des littéraux.

---

*Ce contrat s’applique à l’ensemble des modules (`core`, `spring-jdbc`, `integration`, `examples`).*
