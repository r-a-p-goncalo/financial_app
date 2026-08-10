
This document describes the data we expect to have without making assumptions about where it lives and how their relations are defined.

# Identity

### User

    "a user in the app"

    user_name : str

    password : str


### Role: 

    one of [READ, REFERENCE, WRITE, OWN]

### UserPermission
    
    "represents an access a user has to a financial object"

    user : User

    granted_by : User

    granted_date : DateTime

    role : Role



### FinancialObject

    "represents common logic accross all other defined objects"

	notes : Collection<str>
	
	date_of_creation : DateTime
	
	id : str

    creator : User

    parent_object : FinancialObject, a parent object is used to dynamically inherit permissions and such. It will usually lead to a financial object of the type UserContext

    from : FinancialObject, used for loose copies, as the attributes of this financial object may be incomplete and may need to be resolved when it is get

    ----------------------------------------

    deep_copy() : FinancialObject, returns an whole copy of this financial object, changes to the original won't affect it. This includes dependent objects

    loose_copy() : FinancialObject, returns a loose copy of this financial object, changes to the original will reflect on it, except for the manual changes done in the copy, which will be prioritized over changes done to the original 

# Context

### FinancialContext (extends Financial Object)

	"represents a context, where all the other objects exist. A user always has its original context, but it can create another context based on another. This is how projections are done, as the user may easily make changes or studies that can later be retracted, analogous with branching in git"

    accounts : Collection<Account>

    tags : Collection<Tag>

    tag_rules : Collection<TagRule>


### FinancialContextView

	"Represents a view of a financial context, simulated up to a certain date (regarding automatically generated expenses and so on). It is expected for users to constantly looking for a financial context view for the current date"

    original_context : FinancialContext

    date : Date

    simulated_accounts : Collection<Account>, accounts with the simulations done

### Log  (extends Financial Object)

	"a log that represents the last actions a user has taken in a context, useful for the user to reverse changes or study them"


# Financial Entities


### Entity  (extends Financial Object)

	"represents an entity that has accounts, may owe another entity money, and so on"
	

### Company (extends Entity)

    "represents a company, which may have multiple establishments and more"
	
### Group (extends Entity)

    "describres a group, such as a family"
	
### Individual (extends Entity)

    "describes a single individual"
	

# Money

	
### Unit (extends Financial Object)

    "describes a monetary unit, such as euro"
	
### UnitConversion (extends Financial Object)

    "for two different units, their conversion value, for each different day"
	
### MonetaryValue (extends Financial Object)

    "represents a static monetary value, such as one found in a transaction"

    unit : Unit
    value : Float

    get_value() : float, the internal float value that is stored


### DynamicValue (extends Financial Object)

    "represents a changeable value, such as the value of any property"
	
	get_value() : Collection<Pair<Date, MonetaryValue>>, value in unit for each different day

    get_value(date : Date) : MonetaryValue, value for specific date

### DiscreteDynamicValue(extends Value)

    "represents a value that has registered its initial value and changes at dates to it, without using any specific rule to calculate its changes"
	

# Property

### Asset (extends FinancialObject)

    "represents a non liquid asset, such as a stock or a house"

    get_value() : DynamicValue


### AssetOwnership (extends FinancialObject)

    "represents the ownership of a non liquid asset"

    asset : Asset

    percentage : float [0, 100], the percentage of ownership of the asset

# Programs


### Program (extends FinancialObject)

    "represents something that affects the finances, automatically adding changes to expenses"


### TaxesProgram (extends Program)

    "represents a program that describes the behavior of taxes"


# Helpers

### Variable (extends FinancialObject)

    "a value that may change for each date, used as a helper for things like taxes (how much taxes are we isent from for example)"

    name : str

    get_value() : DynamicValue

    

# Accounts


### AccountRelation (extends Financial Object)

    "represents a relationship an account has with an entity"

    account : Account

    entity : Entity

    type : One of [owns, manages], owning is for the owner, managing is for the bank or other entities that may use it

	
### Account (extends Financial Object)

    "represents an account"

    initial_amount : MonetaryValue

    ----------------------------------------

    get_current_amount(date : DateTime) : MonetaryValue


### AccountGroup(extends Account)

    "represents an account group"

    internal_account : Account, when a transaction is made to an account group with no specified target account, there is an hidden default account that serves as a target

    accounts : Collection<Account>

	
# Transactions


### Transaction (is abstract, extends FinancialObject)

    "Transactions, which are always transfers of money from an account to another, even it the other is unkown or irrelevant"

    date : DateTime

    tags : Collection<Tag>, in the case of composite transactions, it becomes a tag that is added to every sub transaction

    ----------------------------------------

    get_value() : MonetaryValue


### AtomicTransaction (extends Transaction)

    "represents an atomic transaction, which has a value, and the account of origin and target account for the money, a tag. A null account means that it does not matter for the user where the money came or where it went, but at least one of the accounts must be non null"

    from_account : Account?
    to_account : Account?

    value : MonetaryValue


### Composite Transaction (extends Transaction)

    "represents a group of transactions"
	
    transactions : Collection<Transaction>

    unidentified_value : MonetaryValue

    ----------------------------------------

    get_value() : MonetaryValue, the sum of the values of the transactions and the unidentified value
    
    


### Tag (extends FinancialObject)
    "a tag for transactions, meant to help query and study them"


### TagRule (extends FinancialObject)

    "defines that if transaction A is of tag T, it also is of tag Y. We should prohibit circular dependencies"

    super_tag : Tag
    sub_tag : Tag


# Rules

### TransactionCreationRule (extends FinancialObject)

    "Describes a rule that defines possibly new transactions for each day"

    begin_date : Date
    end_date : Date

    ----------------------------------------

    transaction_in_date(context : UserContext, date : Date) : Transaction
    delete_created_transactions()
	

### Filter (extends FinancialObject)

    " a rule that filters out certain accounts, should be close to a description of a database query "