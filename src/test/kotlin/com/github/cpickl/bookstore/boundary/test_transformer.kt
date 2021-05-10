package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.Money

fun Book.toSimpleJson() = """{
    "id": "$id",
    "title": "$title",
    "detailLink": { "method": "GET", "path": "/api/books/$id", "templated": false }
}"""

fun Book.toDetailXml() = """<book>
    <id>$id</id>
    <title><![CDATA[$title]]></title>
    <description><![CDATA[$description]]></description>
    <price>${price.toXml()}</price>
    <author>$authorName</author>
    <coverLink><method>GET</method><path>/api/books/$id/cover</path><templated>false</templated></coverLink>
    <updateLink><method>PUT</method><path>/api/books/$id</path><templated>false</templated></updateLink>
    <deleteLink><method>DELETE</method><path>/api/books/$id</path><templated>false</templated></deleteLink>
</book>"""

fun Book.toDetailJson() = """{
    "id": "$id",
    "title": "$title",
    "description": "$description",
    "price": ${price.toJson()},
    "author": "$authorName",
    "coverLink": { "method": "GET", "path": "/api/books/$id/cover", "templated": false },
    "updateLink": { "method": "PUT", "path": "/api/books/$id", "templated": false },
    "deleteLink": { "method": "DELETE", "path": "/api/books/$id", "templated": false }
}"""

fun Book.toSimpleXml() = """<book>
    <id>$id</id>
    <title>$title</title>
    <detailLink><method>GET</method><path>/api/books/$id</path><templated>false</templated></detailLink>
</book>"""

fun Money.toJson() =
    """{"currencyCode":"${currency.code}","value":$value,"precision":${currency.precision}}"""

fun Money.toXml() =
    """<currencyCode>${currency.code}</currencyCode>
    <value>$value</value>
    <precision>${currency.precision}</precision>"""
