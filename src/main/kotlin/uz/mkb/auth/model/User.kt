package uz.mkb.auth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["username"])])
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    var fullName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Clock.System.now()
)