package xyz.hyperreal.yaml

import xyz.hyperreal.pretty.prettyPrint

object Main extends App {

  val s =
    """|#
       |# auto123.com lead scrape v1.0
       |# does not scrape individual dealer listing pages
       |#
       |
       |start: https://www.auto123.com/en/site-map/ # http://localhost:8080/en/site-map/
       |domain: auto123.com
       |initial: |
       |  write('auto123.com lead scrape v1.0')
       |  write('----------------------------')
       |  write()
       |  scraper.pages = 0
       |  scraper.dealers = table()
       |paths:
       |  - path: /en/site-map/
       |    crawl: true
       |    copy: true
       |  - path: /en/new-cars/dealers/all-makes/[^/]+/
       |    crawl: true
       |    copy: true
       |    action: |
       |      write("listings page: ${$args.url}")
       |      scraper.pages++
       |
       |      $args.body ?
       |        every scan('box_info')
       |          if scan('href="') & (listing = scanto('"'))
       |            if scraper.dealers(listing) == undefined
       |              scraper.dealers(listing) = table({listing: 'https://www.auto123.com' + listing, listings: $args.url})
       |              scan('"title">') & (scraper.dealers(listing).name = scanto('<'))
       |              scan('"address">') & (scraper.dealers(listing).address = scanto('<'))
       |
       |              $args.url ?
       |                if find('alberta') then scraper.dealers(listing).province = 'AB'
       |                else if find('british-columbia') then scraper.dealers(listing).province = 'BC'
       |                else if find('manitoba') then scraper.dealers(listing).province = 'MB'
       |                else if find('new-brunswick') then scraper.dealers(listing).province = 'NB'
       |                else if find('newfoundland') then scraper.dealers(listing).province = 'NL'
       |                else if find('northwest-territories') then scraper.dealers(listing).province = 'NT'
       |                else if find('nova-scotia') then scraper.dealers(listing).province = 'NS'
       |                else if find('ontario') then scraper.dealers(listing).province = 'ON'
       |                else if find('prince-edward-island') then scraper.dealers(listing).province = 'PE'
       |                else if find('quebec') then scraper.dealers(listing).province = 'QC'
       |                else if find('saskatchewan') then scraper.dealers(listing).province = 'SK'
       |                else if find('yukon') then scraper.dealers(listing).province = 'YT'
       |                else error("error extracting province from ${$args.url}")
       |
       |              scan('tel:') & (scraper.dealers(listing).phone = scanto('"'))
       |              scan('mailto:') & (scraper.dealers(listing).email = scanto('"'))
       |          else
       |            error("error scraping ${$args.url}")
       |final: |
       |  write()
       |  write('---------------------')
       |  write("${scraper.pages} listings page(s)")
       |  write('---------------------')
       |
       |  headers(['name', 'phone', 'email', 'address', 'province', 'listing', 'listings'])
       |  export(scraper.dealers)
       |""".trim.stripMargin
//    """|- - a: asdf
//       |    b: sdfg
//       |  - bnm
//    """.trim.stripMargin
//    """|a:
//       | b:
//       |  - 1
//       |  -
//       |   c:
//       |   d: 2
//       |""".stripMargin
//    """|-
//       | - 1
//       | -
//       |  - asdf
//       |- zxcv
//       |""".stripMargin
//    "[asdf, 2, 3]"
  val ast = readFromString(s)

  println(prettyPrint(ast))

}
