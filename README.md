property-manager-ri
===================

A persistent key-value storage with cache support. The data is stored in
relational database.

The module contains a configurable component that registers a PropertyManager
OSGi service. The database schema is defined with Liquibase changelog files.
The name of the liquibase capability is 'org.everit.osgi.props.ri'.

The module is currently tested on H2, Mysql and Postgresql but it should
work on other database engines, too.

For more information please see the [modularized persistence][1] article.

[![Analytics](https://ga-beacon.appspot.com/UA-15041869-4/everit-org/property-manager-api)](https://github.com/igrigorik/ga-beacon)

[1]: http://everitorg.wordpress.com/2014/06/18/modularized-persistence/
