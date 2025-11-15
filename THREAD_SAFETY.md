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

Le dialecte SQL actif est porté par un `ThreadLocal` **pendant la compilation**.  
Toujours encapsuler la compilation dans un scope avec fermeture garantie :

```java
try (var ignored = Dialects.use(Dialects.POSTGRES)) {
  CompiledQuery cq = SqlQuery
      .select(EMP.ID, EMP.NAME)
      .from(EMP)
      .where(EMP.ID).eq(SqlParameter.of("id", Integer.class))
      .compile(); // utilise le dialecte du scope
}
```

> En environnement réactif/async, le `ThreadLocal` ne se propage pas entre threads.  
> Préférer une API explicite (`compile(dialect)`) si disponible, ou s’assurer que la compilation s’effectue dans un contexte monothread.

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
  try (var ignored = Dialects.use(Dialects.POSTGRES)) {
    FIND_EMP_BY_ID = SqlQuery
        .select(EMP.ID, EMP.NAME)
        .from(EMP)
        .where(EMP.ID).eq(SqlParameter.of("id", Integer.class))
        .compile();
  }
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
