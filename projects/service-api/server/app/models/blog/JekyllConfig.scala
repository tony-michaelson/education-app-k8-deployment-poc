package models.blog

case class JekyllSite(
    name: String,
    description: String,
    logo: String,
    favicon: String,
)

case class JekyllConfig(
    site: JekyllSite
)
