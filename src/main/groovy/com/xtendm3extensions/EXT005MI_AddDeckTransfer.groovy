// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add deck register to EXTDPR
// Transaction AddDeckRegister
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: TRNO - Transaction Number
 * @param: TRDT - Transaction Date
 * @param: TRTP - Transaction Type
 * @param: ACCD - Account Code
 * @param: TRRE - Transaction Receipt
 * @param: TRTT - Transaction Ticket
 * @param: LOAD - Load
 * @param: TGBF - Gross BF
 * @param: TNBF - Net BF
 * @param: TRNB - Net Balance
 * 
*/

import java.lang.Math
import java.math.BigDecimal
import java.math.RoundingMode 

public class AddDeckTransfer extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int outTRNO
  int inTRNO
  String inFRYD
  int inFRDK
  String inTOYD
  int inTODK
  int inTRDT 
  int inEXVL 
  int inLOAD
  int inTLOG
  double inTGBF
  double inTNBF 
  String inACCD
  String inACNM
  String inTRRE 
  String inTRTT 
  double fromDetailLOAD
  double fromDetailTLOG
  double fromDetailTNBF
  double fromDetailTGBF
  double toDetailLOAD
  double toDetailTLOG
  double toDetailTNBF
  double toDetailTGBF
  BigDecimal transactionLOAD
  BigDecimal transactionTNBF
  BigDecimal transactionTGBF
  BigDecimal transactionTLOG
  double transactionLOADfrom
  double transactionTNBFfrom
  double transactionTGBFfrom
  double transactionTLOGfrom
  double transactionTRNBfrom
  double transactionLOADto
  double transactionTNBFto
  double transactionTGBFto
  double transactionTLOGto
  double transactionTRNBto
  double transactionTRNB
  int automaticVolume
  String nextNumber
  int fromDeck
  String fromYard
  int toDeck
  String toYard
  boolean foundRecord
  String transactionDate
  int transactionNumber
  int transactionType
  String species
  String referenceNumber
  int invoiceBatchNumber
  String accountCode
  String accountName
  double grossBF
  double netBF
  double netBalance
  double grossWeight
  double tareWeight
  double netWeight
  double loads
  double logs
  int deckID
  int inDPIDupdate
  double inLOADupdate
  double inTLOGupdate
  double inTGBFupdate
  double inTNBFupdate
  double previousTRNB
  double inputTNBF


  // Constructor 
  public AddDeckTransfer(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // From Yard
     if (mi.in.get("FRYD") != null && mi.in.get("FRYD") != "") {
        inFRYD = mi.inData.get("FRYD").trim() 
     } else {
        inFRYD = ""         
     }

     // From Deck
     if (mi.in.get("FRDK") != null) {
        inFRDK = mi.in.get("FRDK") 
     } else {
        inFRDK = 0         
     }

     // To Yard
     if (mi.in.get("TOYD") != null && mi.in.get("TOYD") != "") {
        inTOYD = mi.inData.get("TOYD").trim() 
     } else {
        inTOYD = ""         
     }

     // To Deck
     if (mi.in.get("TODK") != null) {
        inTODK = mi.in.get("TODK") 
     } else {
        inTODK = 0         
     }
   
     // Transaction Date
     if (mi.in.get("TRDT") != null) {
        inTRDT = mi.in.get("TRDT") 
        
        //Validate date format
        boolean validTRDT = utility.call("DateUtil", "isDateValid", String.valueOf(inTRDT), "yyyyMMdd")  
        if (!validTRDT) {
           mi.error("Transaction Date is not valid")   
           return  
        } 

     } else {
        inTRDT = 0        
     }

     // Explicit Volume
     if (mi.in.get("EXVL") != null) {
        inEXVL = mi.in.get("EXVL") 
     } else {
        inEXVL = 0        
     }
     
     // Load
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } else {
        inLOAD = 0        
     }
     
     // Logs
     if (mi.in.get("TLOG") != null) {
        inTLOG = mi.in.get("TLOG") 
     } else {
        inTLOG = 0        
     }

     // Gross BF
     if (mi.in.get("TGBF") != null) {
        inTGBF = mi.in.get("TGBF") 
     } else {
        inTGBF = 0d        
     }
     
     // Net BF
     if (mi.in.get("TNBF") != null) {
        inTNBF = mi.in.get("TNBF") 
        inputTNBF = inTNBF
     } else {
        inTNBF = 0d      
     }
     
     // Set Account Code
     inACCD = mi.in.get("ACCD")
     if (inACCD == null || inACCD == "") {
        accountCode = ""
     }else{
        accountCode = inACCD.trim()
     }
     
     // Set Account Name
     inACNM = mi.in.get("ACNM")
     if (inACNM == null || inACNM == "") {
        accountName = ""
     }else{
        accountName = inACNM.trim()
     }
     
     // Receipt No
     if (mi.in.get("TRRE") != null && mi.in.get("TRRE") != "") {
        inTRRE = mi.inData.get("TRRE").trim() 
     } else {
        inTRRE = ""        
     }
     
     // Trip Ticket No
     if (mi.in.get("TRTT") != null && mi.in.get("TRTT") != "") {
        inTRTT = mi.inData.get("TRTT").trim() 
     } else {
        inTRTT = ""        
     }


     // Validate Deck Head record From Deck
     Optional<DBContainer> fromEXTDPH = findEXTDPH(inCONO, inDIVI, inFRDK, inFRYD)
     if(!fromEXTDPH.isPresent()){
        mi.error("From Deck/Yard record doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPHfrom = fromEXTDPH.get() 
        fromDeck = containerEXTDPHfrom.get("EXDPID") 
        fromYard = containerEXTDPHfrom.get("EXYARD") 
     }
     
     // Validate Deck Head record From Deck
     Optional<DBContainer> toEXTDPH = findEXTDPH(inCONO, inDIVI, inTODK, inTOYD)
     if(!toEXTDPH.isPresent()){
        mi.error("From Deck/Yard record doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPHto = toEXTDPH.get() 
        toDeck = containerEXTDPHto.get("EXDPID") 
        toYard = containerEXTDPHto.get("EXYARD") 
     }


     fromDetailLOAD = 0
     fromDetailTLOG = 0
     fromDetailTGBF = 0d
     fromDetailTNBF = 0d        
     
     // Get Deck Profile Detail record from deck
     Optional<DBContainer> fromEXTDPD = findEXTDPD(inCONO, inDIVI, inFRDK)
     if(!fromEXTDPD.isPresent()){
        mi.error("Deck Profile Details doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPDfrom = fromEXTDPD.get() 
        fromDetailLOAD = containerEXTDPDfrom.get("EXLOAD") 
        fromDetailTLOG = containerEXTDPDfrom.get("EXDLOG") 
        fromDetailTGBF = containerEXTDPDfrom.get("EXGVBF") 
        fromDetailTNBF = containerEXTDPDfrom.get("EXNVBF")         
     } 
     
     toDetailLOAD = 0
     toDetailTLOG = 0
     toDetailTGBF = 0d
     toDetailTNBF = 0d        

     // Get Deck Profile Detail record from deck
     Optional<DBContainer> toEXTDPD = findEXTDPD(inCONO, inDIVI, inTODK)
     if(!toEXTDPD.isPresent()){
        mi.error("Deck Profile Details doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPDto = toEXTDPD.get() 
        toDetailLOAD = containerEXTDPDto.get("EXLOAD") 
        toDetailTLOG = containerEXTDPDto.get("EXDLOG") 
        toDetailTGBF = containerEXTDPDto.get("EXGVBF") 
        toDetailTNBF = containerEXTDPDto.get("EXNVBF")         
     } 


      //Get next number 
      getNextNumber("", "L4", "1") 
      outTRNO = nextNumber as Integer
      inTRNO = outTRNO as Integer
      

      //Get the record with most species for a specific deck from EXTDPR
      foundRecord = false
      listEXTDPRbySPEC()
      

      // FROM *************************************************************************************
      //Get last TRNB for the deck
      previousTRNB = 0d
      List<DBContainer> ResultEXTDPRfromPrevious = listEXTDPR(inCONO, inDIVI, inFRDK, inTRNO)
      for (DBContainer RecLineEXTDPR : ResultEXTDPRfromPrevious){ 
         transactionNumber = RecLineEXTDPR.get("EXTRNO") 
         previousTRNB = RecLineEXTDPR.get("EXTRNB") 
      }

      logger.debug("previousTRNB FROM ${previousTRNB}")

      transactionLOAD = 0d
      transactionTNBF = 0d
      transactionTGBF = 0d
      transactionTLOG = 0d
      automaticVolume = 0
      
      if (inEXVL == 0) {
         if (inLOAD > fromDetailLOAD) {
            transactionLOAD = fromDetailLOAD
         } else {
            transactionLOAD = inLOAD
         }
         if (transactionLOAD > 0 && fromDetailLOAD > 0) {
            transactionTNBF = fromDetailTNBF * (transactionLOAD/fromDetailLOAD)
            transactionTGBF = fromDetailTGBF * (transactionLOAD/fromDetailLOAD)
            transactionTLOG = fromDetailTLOG * (transactionLOAD/fromDetailLOAD)
         } else {
            transactionTNBF = fromDetailTNBF 
            transactionTGBF = fromDetailTGBF
            transactionTLOG = fromDetailTLOG
         }
         automaticVolume = 1
      } else if (inEXVL == 1) {
         if (inLOAD > fromDetailLOAD) {
            transactionLOAD = fromDetailLOAD
         } else {
            transactionLOAD = inLOAD
         }
         transactionTNBF = inTNBF 
         transactionTGBF = inTGBF
         transactionTLOG = inTLOG
         automaticVolume = 0
      }

      logger.debug("transactionTNBF FROM ${transactionTNBF}")
      transactionTRNB = transactionTNBF
      logger.debug("transactionTRNB FROM ${transactionTRNB}")

      transactionLOAD = transactionLOAD.setScale(0, RoundingMode.HALF_UP)   
      transactionTNBF = transactionTNBF.setScale(0, RoundingMode.HALF_UP)   
      transactionTGBF = transactionTGBF.setScale(0, RoundingMode.HALF_UP)   
      transactionTLOG = transactionTLOG.setScale(0, RoundingMode.HALF_UP) 
     
      transactionLOADto = transactionLOAD 
      transactionTNBFto = transactionTNBF
      transactionTGBFto = transactionTGBF
      transactionTLOGto = transactionTLOG

      transactionLOADfrom = -transactionLOAD 
      transactionTNBFfrom = -transactionTNBF
      transactionTGBFfrom = -transactionTGBF
      transactionTLOGfrom = -transactionTLOG
      transactionTRNBfrom = transactionTRNB

      //From Deck/Yard record - minus
      addEXTDPRRecord(inCONO, inDIVI, fromDeck, inTRNO, species, referenceNumber, invoiceBatchNumber, inTRDT, 2, accountCode, accountName, transactionLOADfrom, transactionTLOGfrom, transactionTGBFfrom, transactionTNBFfrom, transactionTRNBfrom, automaticVolume, grossWeight, tareWeight, netWeight, inTRRE, inTRTT)   

      inDPIDupdate = 0
      inLOADupdate = 0d
      inTLOGupdate = 0d
      inTGBFupdate = 0d
      inTNBFupdate = 0d
      
      //Adjust deck details
      inDPIDupdate = inFRDK
      inLOADupdate = fromDetailLOAD-transactionLOAD
      inTLOGupdate = fromDetailTLOG-transactionTLOG
      inTGBFupdate = fromDetailTGBF-transactionTGBF
      inTNBFupdate = fromDetailTNBF-transactionTNBF


      // TO ************************************************************************************************
      //Get last TRNB for the deck
      previousTRNB = 0d
      List<DBContainer> ResultEXTDPRtoPrevious = listEXTDPR(inCONO, inDIVI, inTODK, inTRNO)
      for (DBContainer RecLineEXTDPR : ResultEXTDPRtoPrevious){ 
         transactionNumber = RecLineEXTDPR.get("EXTRNO") 
         previousTRNB = RecLineEXTDPR.get("EXTRNB") 
      }

      logger.debug("previousTRNB TO ${previousTRNB}")
      
      transactionTRNB =  transactionTNBF
      transactionTRNBto = transactionTRNB

      //To Deck/Yard record - plus
      addEXTDPRRecord(inCONO, inDIVI, toDeck, inTRNO, species, referenceNumber, invoiceBatchNumber, inTRDT, 2, accountCode, accountName, transactionLOADto, transactionTLOGto, transactionTGBFto, transactionTNBFto, transactionTRNBto, automaticVolume, grossWeight, tareWeight, netWeight, inTRRE, inTRTT)   

      inDPIDupdate = 0
      inLOADupdate = 0d
      inTLOGupdate = 0d
      inTGBFupdate = 0d
      inTNBFupdate = 0d
      
      //Adjust deck details
      inDPIDupdate = inTODK
      inLOADupdate = toDetailLOAD+transactionLOAD
      inTLOGupdate = toDetailTLOG+transactionTLOG
      inTGBFupdate = toDetailTGBF+transactionTGBF
      inTNBFupdate = toDetailTNBF+transactionTNBF


      mi.outData.put("TRNO", String.valueOf(inTRNO)) 
      mi.write()
  }


   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 


  //******************************************************************** 
  // Get record from EXTDPR with max SPEC
  //******************************************************************** 
  void listEXTDPRbySPEC() {
    DBAction query = database.table("EXTDPR").index("20").selectAllFields().build()
    DBContainer container = query.getContainer()
    container.set("EXCONO", inCONO)
    container.set("EXDIVI", inDIVI)
    container.set("EXDPID", fromDeck)
    
    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(container, 3, pageSize, releasedItemProcessor)
  }

  Closure<?> releasedItemProcessor = { DBContainer container ->
    species = container.getString("EXSPEC")
    deckID = container.get("EXDPID")
    transactionNumber = container.get("EXTRNO")
    transactionDate = container.get("EXTRDT")
    transactionType = container.get("EXTRTP")
    referenceNumber = container.getString("EXTREF")
    invoiceBatchNumber = container.get("EXINBN")
    logs = container.get("EXTLOG")
    loads = container.get("EXLOAD")
    grossBF = container.getDouble("EXTGBF")
    netBF = container.getDouble("EXTNBF")
    netBalance = container.getDouble("EXTRNB")
    grossWeight = container.getDouble("EXGRWE")
    tareWeight = container.getDouble("EXTRWE")
    netWeight = container.getDouble("EXNEWE")
    
    if (!foundRecord) {
       logger.debug("transactionDate ${transactionDate}")
       logger.debug("transactionNumber ${transactionNumber}")
       logger.debug("LOAD ${loads}")
       logger.debug("TGBF ${grossBF}")
       logger.debug("TNBF ${netBF}")
       logger.debug("TRNB ${netBalance}")
       logger.debug("GRWE ${grossWeight}")
       logger.debug("TRWE ${tareWeight}")
       logger.debug("TLOG ${logs}")
       logger.debug("INBN ${invoiceBatchNumber}")
       foundRecord = true
    }

  }  

   //******************************************************************** 
   // Get EXTDPD record
   //******************************************************************** 
   private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID) {  
      DBAction query = database.table("EXTDPD").index("00").selection("EXLOAD", "EXDLOG", "EXGVBF", "EXNVBF").build()
      DBContainer EXTDPD = query.getContainer()
      EXTDPD.set("EXCONO", CONO)
      EXTDPD.set("EXDIVI", DIVI)
      EXTDPD.set("EXDPID", DPID)
      if(query.read(EXTDPD))  { 
        return Optional.of(EXTDPD)
      } 

      return Optional.empty()
   }


   //******************************************************************** 
   // Validate EXTDPH record
   //******************************************************************** 
   private Optional<DBContainer> findEXTDPH(int CONO, String DIVI, int DPID, String YARD){  
      DBAction query = database.table("EXTDPH").index("00").selection("EXDPID", "EXYARD").build()
      DBContainer EXTDPH = query.getContainer()
      EXTDPH.set("EXCONO", CONO)
      EXTDPH.set("EXDIVI", DIVI)
      EXTDPH.set("EXDPID", DPID)
      EXTDPH.set("EXYARD", YARD)
      if(query.read(EXTDPH))  { 
        return Optional.of(EXTDPH)
      } 

      return Optional.empty()
   }
  

  //******************************************************************** 
  // Get last TRNB for the deck
  //********************************************************************  
  private List<DBContainer> listEXTDPR(int CONO, String DIVI, int DPID, int TRNO){  
    List<DBContainer>recLineEXTDPR = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDPR")
    expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXDPID", String.valueOf(DPID)))
    
    DBAction query = database.table("EXTDPR").index("00").matching(expression).selection("EXTRNO", "EXTRNB").reverse().build()
    DBContainer EXTDPR = query.createContainer()
    EXTDPR.set("EXCONO", CONO)
    EXTDPR.set("EXDIVI", DIVI)
    EXTDPR.set("EXDPID", DPID)
    EXTDPR.set("EXTRNO", TRNO)
    
    logger.debug("In qry DPID ${DPID}")
    logger.debug("In qry TRNO ${TRNO}")

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    
    boolean found = false
    query.readAll(EXTDPR, 3, pageSize, { DBContainer recordEXTDPR ->  
       if (!found) {
          recLineEXTDPR.add(recordEXTDPR.createCopy())
          found = true
       }
    })

    return recLineEXTDPR
  }


  //******************************************************************** 
  // Add EXTDPR record 
  //********************************************************************     
  void addEXTDPRRecord(Integer CONO, String DIVI, int DPID, int TRNO, String SPEC, String TREF, int INBN, int TRDT, int TRTP, String ACCD, String ACNM, double LOAD, double TLOG, double TGBF, double TNBF, double TRNB, int AUWE, double GRWE, double TRWE, double NEWE, String TRRE, String TRTT){     
       DBAction action = database.table("EXTDPR").index("00").build()
       DBContainer EXTDPR = action.createContainer()
       EXTDPR.set("EXCONO", CONO)
       EXTDPR.set("EXDIVI", DIVI)
       EXTDPR.set("EXDPID", DPID)
       EXTDPR.set("EXTRNO", TRNO)
       EXTDPR.set("EXSPEC", SPEC)
       EXTDPR.set("EXTREF", TREF)
       EXTDPR.set("EXINBN", INBN)
       EXTDPR.set("EXTRDT", TRDT)
       EXTDPR.set("EXTRTP", TRTP)
       EXTDPR.set("EXACCD", ACCD)
       EXTDPR.set("EXACNM", ACNM)
       EXTDPR.set("EXLOAD", LOAD)
       EXTDPR.set("EXTLOG", TLOG)
       EXTDPR.set("EXTGBF", TGBF)
       EXTDPR.set("EXTNBF", TNBF)
       EXTDPR.set("EXAUWE", AUWE)
       EXTDPR.set("EXGRWE", GRWE)
       EXTDPR.set("EXTRWE", TRWE)
       EXTDPR.set("EXNEWE", NEWE)
       EXTDPR.set("EXTRRE", TRRE)
       EXTDPR.set("EXTRTT", TRTT)
       EXTDPR.set("EXCHID", program.getUser())
       EXTDPR.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDPR.set("EXRGDT", regdate) 
       EXTDPR.set("EXLMDT", regdate) 
       EXTDPR.set("EXRGTM", regtime)
       action.insert(EXTDPR)         
 } 


} 

