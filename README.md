# azure-rest-examples

Examples on using Azure's REST API.  Primary focus will be on the APIs ported to ARM
and JSON based APIs.  Not all APIs have been ported to the new method.

For instance, if you use an HTTPS proxy to sniff traffic using the Azure CLI, you will 
see that, for now (as of 8/1/2015) storage APIs use the older method while ARM and 
Compute use the REST/JSON based apis.

The examples will be in the languages I happen to be using at the time.  Currently, 
the Java SDK has not been released supporting ARM yet.