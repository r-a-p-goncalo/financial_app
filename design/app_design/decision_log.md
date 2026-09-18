
# ADR-001

"Tag" was chosen over "Category" due to its more general meaning. It is more intuitive that an expense has more than one "Tag", over more than one "Category", which is the intended behavior.

Later, when studying expenses, categories can be logically created, where root tags and the possible combinations of them are treated as categories. For example, a tag `Food` can be a category that is not inherently of any other category, as well as `Fun`. If there is at least at one expense which ends up tagged with `Food` and `Fun`, the category `Food and Fun` is created. Subcategories of these joint categories can be then treated with the same logic.

# ADR-002

Initial technology choice: java, spring and react, is made due only to convenience, without any though for what is best.

# ADR-003

We decided to create to make it so the Financial Contexts, and other objects,
are copied almost totally lazily. That is, there is no actual copy until the point a change is made that
creates a need for a distinct record.

# ADR-004

We decided to let the responsibility of knowing when to create a copy be mostly for the client.
This is because it seemed to be adding to much pressure to the server checking if it should actualize or not the copies.