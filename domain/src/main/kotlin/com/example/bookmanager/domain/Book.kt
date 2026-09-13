package com.example.bookmanager.domain

import com.example.bookmanager.shared.Id

/**
 * 書籍エンティティ（集約ルート）。
 */
data class Book(
    val id: BookId,
    val title: Title,
    val price: Price,
    val publishStatus: PublishStatus = PublishStatus.UNPUBLISHED,
    val authorIds: Set<Id>
) {
    companion object {
        fun create(
            title: Title,
            price: Price,
            publishStatus: PublishStatus = PublishStatus.UNPUBLISHED,
            authorIds: Collection<Id>
        ): Book {
            if (authorIds.isEmpty()) {
                throw DomainException(DomainErrorCode.NO_AUTHORS, "Book must have at least one author")
            }
            return Book(
                id = BookId.generate(),
                title = title,
                price = price,
                publishStatus = publishStatus,
                authorIds = authorIds.toSet()
            )
        }

        fun ofExisting(
            id: BookId,
            title: Title,
            price: Price,
            publishStatus: PublishStatus,
            authorIds: Set<Id>
        ): Book {
            return Book(
                id = id,
                title = title,
                price = price,
                publishStatus = publishStatus,
                authorIds = authorIds
            )
        }
    }

    fun withTitle(newTitle: Title): Book = copy(title = newTitle)

    fun withPrice(newPrice: Price): Book = copy(price = newPrice)

    fun withPublishStatus(newStatus: PublishStatus): Book {
        if (!publishStatus.canTransitionTo(newStatus)) {
            throw DomainException(
                DomainErrorCode.INVALID_PUBLISH_STATUS_TRANSITION,
                "Cannot change publish status from $publishStatus to $newStatus"
            )
        }
        return copy(publishStatus = newStatus)
    }

    fun withAuthors(authorIds: Collection<Id>): Book =
        copy(authorIds = normalizeAuthors(authorIds))
}

private fun normalizeAuthors(authorIds: Collection<Id>): Set<Id> {

    return authorIds.toSet()
}
