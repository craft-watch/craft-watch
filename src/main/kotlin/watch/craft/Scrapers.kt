package watch.craft

import watch.craft.scrapers.*
import java.net.URI

private val entry = ::ScraperEntry

val SCRAPERS = listOf(
  entry(
    BeakScraper(),
    Brewery(
      shortName = "Beak",
      name = "Beak Brewery",
      location = "Lewes, East Sussex",
      websiteUrl = URI("https://beakbrewery.com/"),
      twitterHandle = "TheBeakBrewery"
    )
  ),
  entry(
    BoxcarScraper(),
    Brewery(
      shortName = "Boxcar",
      name = "Boxcar Brewery",
      location = "Bethnal Green, London",
      websiteUrl = URI("https://boxcarbrewery.co.uk/"),
      twitterHandle = "BoxcarBrwCo"
    )
  ),
  entry(
    BrickScraper(),
    Brewery(
      shortName = "Brick",
      name = "Brick Brewery",
      location = "Peckham, London",
      websiteUrl = URI("https://www.brickbrewery.co.uk/"),
      twitterHandle = "brick_brewery"
    )
  ),
  entry(
    CanopyScraper(),
    Brewery(
      shortName = "Canopy",
      name = "Canopy Beer Co",
      location = "Brockwell, London",
      websiteUrl = URI("https://www.canopybeer.com/"),
      twitterHandle = "CanopyBeerCo"
    )
  ),
  entry(
    CloudwaterScraper(),
    Brewery(
      shortName = "Cloudwater",
      name = "Cloudwater Brew Co",
      location = "Manchester",
      websiteUrl = URI("https://cloudwaterbrew.co/"),
      twitterHandle = "cloudwaterbrew"
    )
  ),
  entry(
    DeyaScraper(),
    Brewery(
      shortName = "DEYA",
      name = "DEYA Brewing Co",
      location = "Cheltenham, Gloucestershire",
      websiteUrl = URI("https://deyabrewing.com/"),
      twitterHandle = "deyabrewery"
    )
  ),
  entry(
    FivePointsScraper(),
    Brewery(
      shortName = "Five Points",
      name = "The Five Points Brewing Co",
      location = "Hackney, London",
      websiteUrl = URI("https://fivepointsbrewing.co.uk/"),
      twitterHandle = "FivePointsBrew"
    )
  ),
  entry(
    ForestRoadScraper(),
    Brewery(
      shortName = "Forest Road",
      name = "Forest Road Brewing Co",
      location = "Hackney, London",
      websiteUrl = URI("https://www.forestroad.co.uk/"),
      twitterHandle = "ForestRoadBrew"
    )
  ),
  entry(
    FourpureScraper(),
    Brewery(
      shortName = "Fourpure",
      name = "Fourpure Brewing Co",
      location = "Bermondesy, London",
      websiteUrl = URI("https://www.fourpure.com/"),
      twitterHandle = "fourpurebrewing"
    )
  ),
  entry(
    GipsyHillScraper(),
    Brewery(
      shortName = "Gipsy Hill",
      name = "Gipsy Hill Brewing",
      location = "Gispy Hill, London",
      websiteUrl = URI("https://gipsyhillbrew.com/"),
      twitterHandle = "GipsyHillBrew"
    )
  ),
  entry(
    HackneyChurchScraper(),
    Brewery(
      shortName = "Hackney Church",
      name = "Hackney Church Brew Co",
      location = "Hackney, London",
      websiteUrl = URI("https://hackneychurchbrew.co/"),
      twitterHandle = "hackneychurchbc"
    )
  ),
  entry(
    HowlingHopsScraper(),
    Brewery(
      shortName = "Howling Hops",
      name = "Howling Hops",
      location = "Hackney Wick, London",
      websiteUrl = URI("https://www.howlinghops.co.uk/"),
      twitterHandle = "HowlingHops"
    )
  ),
  entry(
    InnisAndGunnScraper(),
    Brewery(
      shortName = "Innis & Gunn",
      name = "Innis & Gunn Brewing Co",
      location = "Edinburgh",
      websiteUrl = URI("https://www.innisandgunn.com/"),
      twitterHandle = "innisandgunn"
    )
  ),
  entry(
    JeffersonsScraper(),
    Brewery(
      shortName = "Jeffersons",
      name = "Jeffersons Brewery",
      location = "Barnes, London",
      websiteUrl = URI("https://jeffersonsbrewery.co.uk/"),
      twitterHandle = "JeffersonsBeers"
    )
  ),
  entry(
    MarbleScraper(),
    Brewery(
      shortName = "Marble",
      name = "Marble Beers",
      location = "Salford, Greater Manchester",
      websiteUrl = URI("https://marblebeers.com/"),
      twitterHandle = "marblebrewers"
    )
  ),
  entry(
    NorthernMonkScraper(),
    Brewery(
      shortName = "Northern Monk",
      name = "Northern Monk Brew Co",
      location = "Holbeck, Leeds",
      websiteUrl = URI("https://northernmonk.com/"),
      twitterHandle = "NMBCo"
    )
  ),
  entry(
    OrbitScraper(),
    Brewery(
      shortName = "Orbit",
      name = "Orbit Beers",
      location = "Walworth, London",
      websiteUrl = URI("https://www.orbitbeers.com/"),
      twitterHandle = "OrbitBeers"
    )
  ),
  entry(
    PadstowScraper(),
    Brewery(
      shortName = "Padstow",
      name = "Padstow Brewing Co",
      location = "Padstow, Cornwall",
      websiteUrl = URI("https://www.padstowbrewing.co.uk/"),
      twitterHandle = "PadstowBrewing"
    )
  ),
  entry(
    PillarsScraper(),
    Brewery(
      shortName = "Pillars",
      name = "Pillars Brewery",
      location = "Walthamstow, London",
      websiteUrl = URI("https://www.pillarsbrewery.com/"),
      twitterHandle = "PillarsBrewery"
    )
  ),
  entry(
    PollysScraper(),
    Brewery(
      shortName = "Polly's Brew",
      name = "Polly's Brew Co",
      location = "Mold, Flintshire",
      websiteUrl = URI("https://pollysbrew.co/"),
      twitterHandle = "pollysbrewco"
    )
  ),
  entry(
    PressureDropScraper(),
    Brewery(
      shortName = "Pressure Drop",
      name = "Pressure Drop Brewing",
      location = "Tottenham, London",
      websiteUrl = URI("https://pressuredropbrewing.co.uk/"),
      twitterHandle = "PressureDropBrw"
    )
  ),
  entry(
    RedchurchScraper(),
    Brewery(
      shortName = "Redchurch",
      name = "Redchurch Brewery",
      location = "Harlow, Essex",
      websiteUrl = URI("https://redchurch.beer/"),
      twitterHandle = "Redchurchbrewer"
    )
  ),
  entry(
    RedWillowScraper(),
    Brewery(
      shortName = "RedWillow",
      name = "RedWillow Brewery",
      location = "Macclesfield, Cheshire",
      websiteUrl = URI("https://www.redwillowbrewery.com/"),
      twitterHandle = "redwillowbrew"
    )
  ),
  entry(
    RoostersScraper(),
    Brewery(
      shortName = "Rooster's",
      name = "Rooster's Brewing Co",
      location = "Harrogate, North Yorkshire",
      websiteUrl = URI("https://www.roosters.co.uk/"),
      twitterHandle = "RoostersBrewCo"
    )
  ),
  entry(
    SirenScraper(),
    Brewery(
      shortName = "Siren Craft",
      name = "Siren Craft Brew",
      location = "Finchampstead, Berkshire",
      websiteUrl = URI("https://www.sirencraftbrew.com/"),
      twitterHandle = "SirenCraftBrew"
    )
  ),
  entry(
    SolvayScraper(),
    Brewery(
      shortName = "Solvay Society",
      name = "Solvay Society",
      location = "Leytonstone, London",
      websiteUrl = URI("https://www.solvaysociety.com/"),
      twitterHandle = "SolvaySociety"
    )
  ),
  entry(
    StewartScraper(),
    Brewery(
      shortName = "Stewart Brewing",
      name = "Stewart Brewing",
      location = "Loanhead, Midlothian",
      websiteUrl = URI("https://www.stewartbrewing.co.uk/"),
      twitterHandle = "StewartBrewing"
    )
  ),
  entry(
    ThornbridgeScraper(),
    Brewery(
      shortName = "Thornbridge",
      name = "Thornbridge Brewery",
      location = "Bakewell, Derbyshire",
      websiteUrl = URI("https://thornbridgebrewery.co.uk/"),
      twitterHandle = "thornbridge"
    )
  ),
  entry(
    UnityScraper(),
    Brewery(
      shortName = "Unity",
      name = "Unity Brewing Co",
      location = "Southampton",
      websiteUrl = URI("https://unitybrewingco.com/"),
      twitterHandle = "unitybrewingco"
    )
  ),
  entry(
    VerdantScraper(),
    Brewery(
      shortName = "Verdant",
      name = "Verdant Brewing Co",
      location = "Penryn, Cornwall",
      websiteUrl = URI("https://verdantbrewing.co/"),
      twitterHandle = "VerdantBrew"
    )
  ),
  entry(
    VillagesScraper(),
    Brewery(
      shortName = "Villages",
      name = "Villages Brewery",
      location = "Deptford, London",
      websiteUrl = URI("https://villagesbrewery.com/"),
      twitterHandle = "VillagesBrewery"
    )
  ),
  entry(
    WanderScraper(),
    Brewery(
      shortName = "Wander Beyond",
      name = "Wander Beyond Brewing",
      location = "Manchester",
      websiteUrl = URI("https://www.wanderbeyondbrewing.com/"),
      twitterHandle = "wanderbeyond_"
    )
  ),
  entry(
    WiperAndTrueScraper(),
    Brewery(
      shortName = "Wiper and True",
      name = "Wiper and True",
      location = "Bristol",
      websiteUrl = URI("https://wiperandtrue.com/"),
      twitterHandle = "WiperAndTrue"
    )
  ),
  entry(
    WylamScraper(),
    Brewery(
      shortName = "Wylam",
      name = "Wylam Brewery",
      location = "Newcastle upon Tyne",
      websiteUrl = URI("https://www.wylambrewery.co.uk/"),
      twitterHandle = "wylambrewery"
    )
  )
)

