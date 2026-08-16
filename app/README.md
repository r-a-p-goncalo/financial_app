Here we have implementation details about the currently implemented app version.

# App version 0.1

The CLI package serves as the interface for the user, dealing only with its inputs and outputs.

The client package defines the requests and logic of the requests the user makes to the server app.

The application package serves as the server side logic, dealing with data validation, certain computations and 

This version assumes that when a value is null, it is yet to be loaded. In the future, distinction between loaded, unloaded, non-existent and prohibited attributes will have to be implemented.

