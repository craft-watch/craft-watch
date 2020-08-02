package watch.craft

import watch.craft.scrapers.*
import java.net.URI

private val entry = ::ScraperEntry

val SCRAPERS = listOf(
  entry(
    AffinityScraper(),
    Brewery(
      id = "affinity",
      shortName = "Affinity",
      name = "Affinity Brew Co",
      location = "Bermondsey, London",
      websiteUrl = URI("https://affinitybrewco.com/"),
      twitterHandle = "AffinityBrewCo"
    )
  ),
  entry(
    AnspachAndHobdayScraper(),
    Brewery(
      id = "anspach-and-hobday",
      shortName = "Anspach & Hobday",
      name = "Anspach & Hobday",
      location = "Bermondsey, London",
      websiteUrl = URI("https://www.anspachandhobday.com/"),
      twitterHandle = "AnspachHobday"
    )
  ),
  entry(
    BeakScraper(),
    Brewery(
      id = "beak",
      shortName = "Beak",
      name = "Beak Brewery",
      location = "Lewes, East Sussex",
      websiteUrl = URI("https://beakbrewery.com/"),
      twitterHandle = "TheBeakBrewery"
    )
  ),
  entry(
    BigDropScraper(),
    Brewery(
      id = "big-drop",
      shortName = "Big Drop",
      name = "Big Drop Brewing Co",
      location = "Ipswich, Suffolk",
      websiteUrl = URI("https://www.bigdropbrew.com/"),
      twitterHandle = "bigdropbrewco"
    )
  ),
  entry(
    BoxcarScraper(),
    Brewery(
      id = "boxcar",
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
      id = "brick",
      shortName = "Brick",
      name = "Brick Brewery",
      location = "Peckham, London",
      websiteUrl = URI("https://www.brickbrewery.co.uk/"),
      twitterHandle = "brick_brewery"
    )
  ),
  entry(
    BrockleyScraper(),
    Brewery(
      id = "brockley",
      shortName = "Brockley",
      name = "Brockley Brewing Co",
      location = "Brockley, London",
      websiteUrl = URI("https://www.brockleybrewery.co.uk/"),
      twitterHandle = "brockleybrewery"
    )
  ),
  entry(
    BurningSkyScraper(),
    Brewery(
      id = "burning-sky",
      shortName = "Burning Sky",
      name = "Burning Sky",
      location = "Firle, East Sussex",
      websiteUrl = URI("https://www.burningskybeer.com"),
      twitterHandle = "burningskybeer"
    )
  ),
  entry(
    BurntMillScraper(),
    Brewery(
      id = "burnt-mill",
      shortName = "Burnt Mill",
      name = "Burnt Mill Brewery",
      location = "Suffolk",
      websiteUrl = URI("https://burnt-mill-brewery.myshopify.com/"),
      twitterHandle = "BurntMillBeer"
    )
  ),
  entry(
    CanopyScraper(),
    Brewery(
      id = "canopy",
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
      id = "cloudwater",
      shortName = "Cloudwater",
      name = "Cloudwater Brew Co",
      location = "Manchester",
      websiteUrl = URI("https://cloudwaterbrew.co/"),
      twitterHandle = "cloudwaterbrew"
    )
  ),
  entry(
    CraftyScraper(),
    Brewery(
      id = "crafty-brewing",
      shortName = "Crafty Brewing",
      name = "Crafty Brewing Co",
      location = "Godalming, Surrey",
      websiteUrl = URI("https://www.craftybrewing.co.uk/"),
      twitterHandle = "craftybrewing"
    )
  ),
  entry(
    DeyaScraper(),
    Brewery(
      id = "deya",
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
      id = "five-points",
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
      id = "forest-road",
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
      id = "fourpure",
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
      id = "gipsy-hill",
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
      id = "hackney-church",
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
      id = "howling-hops",
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
      id = "innis-and-gunn",
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
      id = "jeffersons",
      shortName = "Jeffersons",
      name = "Jeffersons Brewery",
      location = "Barnes, London",
      websiteUrl = URI("https://jeffersonsbrewery.co.uk/"),
      twitterHandle = "JeffersonsBeers"
    )
  ),
  entry(
    MadSquirrelScraper(),
    Brewery(
      id = "mad-squirrel",
      shortName = "Mad Squirrel",
      name = "Mad Squirrel",
      location = "Potten End, Hertfordshire",
      websiteUrl = URI("https://www.madsquirrelbrew.co.uk/"),
      twitterHandle = "marblebrewers"
    )
  ),
  entry(
    MarbleScraper(),
    Brewery(
      id = "marble",
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
      id = "northern-monk",
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
      id = "orbit",
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
      id = "padstow",
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
      id = "pillars",
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
      id = "pollys-brew",
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
      id = "pressure-drop",
      shortName = "Pressure Drop",
      name = "Pressure Drop Brewing",
      location = "Tottenham, London",
      websiteUrl = URI("https://pressuredropbrewing.co.uk/"),
      twitterHandle = "PressureDropBrw"
    )
  ),
  entry(
    PurityScraper(),
    Brewery(
      id = "purity",
      shortName = "Purity",
      name = "Purity Brewing Co",
      location = "Great Alne, Warwickshire",
      websiteUrl = URI("https://puritybrewing.com/"),
      twitterHandle = "Purityale"
    )
  ),
  entry(
    RedchurchScraper(),
    Brewery(
      id = "redchurch",
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
      id = "redwillow",
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
      id = "roosters",
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
      id = "siren-craft",
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
      id = "solvay-society",
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
      id = "stewart-brewing",
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
      id = "thornbridge",
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
      id = "unity",
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
      id = "verdant",
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
      id = "villages",
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
      id = "wander-beyond",
      shortName = "Wander Beyond",
      name = "Wander Beyond Brewing",
      location = "Manchester",
      websiteUrl = URI("https://www.wanderbeyondbrewing.com/"),
      twitterHandle = "wanderbeyond_"
    )
  ),
  entry(
    WildBeerScraper(),
    Brewery(
      id = "wild-beer",
      shortName = "Wild Beer",
      name = "Wild Beer Co",
      location = "Westcombe, Somerset",
      websiteUrl = URI("https://www.wildbeerco.com/"),
      twitterHandle = "WildBeerCo"
    )
  ),
  entry(
    WildCardScraper(),
    Brewery(
      id = "wild-card",
      shortName = "Wild Card",
      name = "Wild Card Brewery",
      location = "Walthamstow, London",
      websiteUrl = URI("https://www.wildcardbrewery.co.uk/"),
      twitterHandle = "WildCardBrewery"
    )
  ),
  entry(
    WiperAndTrueScraper(),
    Brewery(
      id = "wiper-and-true",
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
      id = "wylam",
      shortName = "Wylam",
      name = "Wylam Brewery",
      location = "Newcastle upon Tyne",
      websiteUrl = URI("https://www.wylambrewery.co.uk/"),
      twitterHandle = "wylambrewery"
    )
  )
)

