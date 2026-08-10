
# ADR-001

"Tag" was chosen ove "Category" due to its more general meaning. It is more intuitive that an expense has more than one "Tag", over more than one "Category", which is the intended behavior.

Later, when studying expenses, categories can be logicaly created, where root tags and the possible combinations of them are treated as categories. For example, a tag `Food` can be a category that is not inherintely of any other category, as well as `Fun`. If there is at least at one expense which ends up tagged with `Food` and `Fun`, the category `Food and Fun` is created. Subcategories of these joint cantegories can be then treated with the same logic.

# ADR-002

Initial technology choice: java, spring and react, is made due only to convinence, without any though for what is best.