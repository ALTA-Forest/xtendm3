// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add deck adjustment to EXTDPR
// Transaction AddDeckAdjstmnt
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: TRDT - Transaction Date
 * @param: ISPC - Is Percentage
 * @param: EINV - Effect on Inventory
 * @param: PCTG - Percentage
 * @param: LOAD - Loads
 * @param: TLOG - Logs
 * @param: TGBF - Gross BF
 * @param: TNBF - Net BF
 * 
*/


import java.lang.Math
import java.math.BigDecimal
import java.math.RoundingMode 


public class AddDeckAdjstmnt extends ExtendM3Transaction {
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
  int inDPID 
  double detailLOAD
  double detailDLOG
  double detailGVBF
  double detailNVBF
  int automaticVolume
  BigDecimal transactionLOAD
  BigDecimal transactionTNBF
  BigDecimal transactionTGBF
  BigDecimal transactionTLOG
  BigDecimal averageLOAD
  BigDecimal averageTNBF
  BigDecimal averageTGBF
  BigDecimal averageTLOG
  double inLOADupdate
  double inTLOGupdate
  double inTGBFupdate
  double inTNBFupdate
  String nextNumber
  boolean foundRecord
  String transactionDate
  String transactionNumber
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
  double newTRNB
  double previousTRNB
  double inputTNBF
  int transactionNumberInt
  double newTRNBadjust

  
  // Constructor 
  public AddDeckAdjstmnt(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0        
     }
  
     // Transaction Date
     int inTRDT 
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

     // Is Percentage
     int inISPC 
     if (mi.in.get("ISPC") != null) {
        inISPC = mi.in.get("ISPC") 
     } else {
        inISPC = 0        
     }

     // Effect on Inventory
     int inEINV 
     if (mi.in.get("EINV") != null) {
        inEINV = mi.in.get("EINV") 
     } else {
        inEINV = 0        
     }
 
     // Percentage
     double inPCTG
     if (mi.in.get("PCTG") != null) {
        inPCTG = mi.in.get("PCTG") 
     } else {
        inPCTG = 0        
     }
    
     // Load
     double inLOAD
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } else {
        inLOAD = 0d        
     }

     // Logs
     double inTLOG
     if (mi.in.get("TLOG") != null) {
        inTLOG = mi.in.get("TLOG") 
     } else {
        inTLOG = 0d        
     }

     // Gross BF
     double inTGBF
     if (mi.in.get("TGBF") != null) {
        inTGBF = mi.in.get("TGBF") 
     } else {
        inTGBF = 0d        
     }
     
     // Net BF
     double inTNBF 
     if (mi.in.get("TNBF") != null) {
        inTNBF = mi.in.get("TNBF") 
        inputTNBF = inTNBF
     } else {
        inTNBF = 0d      
     }


     // Validate Deck Head record 
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if(!EXTDPH.isPresent()){
        mi.error("From Deck/Yard record doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPH = EXTDPH.get() 
     }
     
     detailLOAD = 0
     detailDLOG = 0
     detailGVBF = 0d
     detailNVBF = 0d
     
     // Get Deck Profile Detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if(!EXTDPD.isPresent()){
        mi.error("Deck Profile Details doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPD = EXTDPD.get() 
        detailLOAD = containerEXTDPD.get("EXLOAD") 
        detailDLOG = containerEXTDPD.get("EXDLOG") 
        detailGVBF = containerEXTDPD.get("EXGVBF") 
        detailNVBF = containerEXTDPD.get("EXNVBF") 
     } 

     //Get next number for contract number
     getNextNumber("", "L4", "1") 
     outTRNO = nextNumber as Integer
     inTRNO = outTRNO as Integer

     newTRNB = 0d
     previousTRNB = 0d    

     //Get last TRNB for the deck
     List<DBContainer> ResultEXTDPR = listEXTDPR(inCONO, inDIVI, inDPID, inTRNO)
     for (DBContainer RecLineEXTDPR : ResultEXTDPR){ 
        transactionNumber = RecLineEXTDPR.get("EXTRNO") 
        previousTRNB = RecLineEXTDPR.get("EXTRNB") 
     }
     newTRNB = inputTNBF + previousTRNB
     
     logger.debug("PreviousTRNB ${previousTRNB}")
     logger.debug("InputTNBF ${inputTNBF}")
     logger.debug("NewTRNB ${newTRNB}")
     logger.debug("transactionNumber ${transactionNumber}")


     //Get the record with most species for a specific deck from EXTDPR
     foundRecord = false
     listEXTDPRbySPEC()      
       
     averageLOAD = 0d
     averageTLOG = 0d
     averageTGBF = 0d
     averageTNBF = 0d
      
     logger.debug("inLOAD ${inLOAD}")
     logger.debug("inTLOG ${inTLOG}")
     logger.debug("inTGBF ${inTGBF}")
     logger.debug("inTNBF ${inTNBF}")
      
     logger.debug("detailLOAD ${detailLOAD}")
     logger.debug("detailDLOG ${detailDLOG}")
     logger.debug("detailGVBF ${detailGVBF}")
     logger.debug("detailNVBF ${detailNVBF}")

      
      //Calculate average
      if (inLOAD > 0 && detailLOAD > 0) {
         averageLOAD = inLOAD/detailLOAD
      } else {
         averageLOAD = 1
      }
      if (inTLOG > 0 && detailDLOG > 0) {
         averageTLOG = inTLOG/detailDLOG
      } else {
         averageTLOG = 1     
      }
      if (inTGBF > 0 && detailGVBF > 0) {
         averageTGBF = inTGBF/detailGVBF
      } else {
         averageTGBF = 1
      }
      if (inTNBF > 0 && detailNVBF > 0) {
         averageTNBF = inTNBF/detailNVBF
      } else {
         averageTNBF = 1
      }
         
      logger.debug("averageLOAD ${averageLOAD}")
      logger.debug("averageTLOG ${averageTLOG}")
      logger.debug("averageTGBF ${averageTGBF}")
      logger.debug("averageTNBF ${averageTNBF}")
     
      transactionLOAD = 0d
      transactionTNBF = 0d
      transactionTGBF = 0d
      transactionTLOG = 0d
      automaticVolume = 0
      
      if (inISPC == 0) {
         if (inTNBF != 0d) {
            transactionTNBF = inTNBF
         } else {
            transactionTNBF = averageTNBF
         }
         
         if (inTGBF != 0d) {
            transactionTGBF = inTGBF
         } else {
            transactionTGBF = averageTGBF
         } 
         
         if (inTLOG != 0d) {
            transactionTLOG = inTLOG
         } else {
            transactionTLOG = averageTLOG
         }
         
         if (inLOAD != 0d) {
            transactionLOAD = inLOAD
         } else {
            transactionLOAD = averageLOAD
         }
                 
      } else if (inISPC == 1) {
            transactionTNBF = detailNVBF * (inPCTG/100)
            transactionTGBF = detailGVBF * (inPCTG/100)
            transactionTLOG = detailDLOG * (inPCTG/100)
            transactionLOAD = detailLOAD * (inPCTG/100)
      }
      
      logger.debug("transactionLOAD ${transactionLOAD}")
      logger.debug("transactionTNBF ${transactionTNBF}")
      logger.debug("transactionTGBF ${transactionTGBF}")
      logger.debug("transactionTLOG ${transactionTLOG}")

      transactionLOAD = transactionLOAD.setScale(0, RoundingMode.HALF_UP)   
      transactionTNBF = transactionTNBF.setScale(0, RoundingMode.HALF_UP)   
      transactionTGBF = transactionTGBF.setScale(0, RoundingMode.HALF_UP)   
      transactionTLOG = transactionTLOG.setScale(0, RoundingMode.HALF_UP) 

      logger.debug("transactionLOAD rounded ${transactionLOAD}")
      logger.debug("transactionTNBF rounded ${transactionTNBF}")
      logger.debug("transactionTGBF rounded ${transactionTGBF}")
      logger.debug("transactionTLOG rounded ${transactionTLOG}")

      double transactionTNBFadjust = transactionTNBF
      double transactionTGBFadjust = transactionTGBF
      double transactionTLOGadjust = transactionTLOG
      double transactionLOADadjust = transactionLOAD    
      newTRNBadjust = 0d  
      
      species = ""
      referenceNumber = ""
      invoiceBatchNumber = 0
      accountCode = ""
      accountName = ""
      
      logger.debug("transactionLOAD before write to DPR ${transactionLOAD}")
      logger.debug("transactionTNBF before write to DPR ${transactionTNBF}")
      logger.debug("transactionTGBF before write to DPR ${transactionTGBF}")
      logger.debug("transactionTLOG before write to DPR ${transactionTLOG}")
      logger.debug("transactionTLOG before write to DPR ${transactionTLOG}")
      logger.debug("previousTRNB before write to DPR ${transactionTLOG}")
      logger.debug("transactionTLOG before write to DPR ${transactionTLOG}")
      
      if (inEINV == 0) {
         newTRNBadjust = transactionTNBFadjust
         addEXTDPRRecord(inCONO, inDIVI, inDPID, inTRNO, species, referenceNumber, invoiceBatchNumber, inTRDT, 3, accountCode, accountName, -transactionLOADadjust, -transactionTLOGadjust, -transactionTGBFadjust, -transactionTNBFadjust, newTRNBadjust, automaticVolume, -grossWeight, -tareWeight, -netWeight)           
      } else if (inEINV == 1) {
         newTRNBadjust = transactionTNBFadjust
         addEXTDPRRecord(inCONO, inDIVI, inDPID, inTRNO, species, referenceNumber, invoiceBatchNumber, inTRDT, 3, accountCode, accountName, transactionLOADadjust, transactionTLOGadjust, transactionTGBFadjust, transactionTNBFadjust, newTRNBadjust, automaticVolume, grossWeight, tareWeight, netWeight)   
      }
      
      inLOADupdate = 0d
      inTLOGupdate = 0d
      inTGBFupdate = 0d
      inTNBFupdate = 0d

      if (inEINV == 0) {
        inLOADupdate = detailLOAD-transactionLOADadjust
        inTLOGupdate = detailDLOG-transactionTLOGadjust
        inTGBFupdate = detailGVBF-transactionTGBFadjust
        inTNBFupdate = detailNVBF-transactionTNBFadjust
      } else if (inEINV == 1) {
        inLOADupdate = detailLOAD+transactionLOADadjust
        inTLOGupdate = detailDLOG+transactionTLOGadjust
        inTGBFupdate = detailGVBF+transactionTGBFadjust
        inTNBFupdate = detailNVBF+transactionTNBFadjust        
      }

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
    DBAction query = database.table("EXTDPR").index("20").selectAllFields().reverse().build()
    DBContainer container = query.getContainer()
    container.set("EXCONO", inCONO)
    container.set("EXDIVI", inDIVI)
    
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
    accountCode = container.getString("EXACCD")
    accountName = container.getString("EXACNM")
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
   private Optional<DBContainer> findEXTDPH(int CONO, String DIVI, int DPID){  
      DBAction query = database.table("EXTDPH").index("00").build()
      DBContainer EXTDPH = query.getContainer()
      EXTDPH.set("EXCONO", CONO)
      EXTDPH.set("EXDIVI", DIVI)
      EXTDPH.set("EXDPID", DPID)
      if(query.read(EXTDPH))  { 
        return Optional.of(EXTDPH)
      } 

      return Optional.empty()
   }
  

   //***************************************************************************** 
   // Update Deck Details
   //***************************************************************************** 
   void updDeckDetailsMI(String company, String division, String deckID, String load, String log, String grossVolume, String netVolume, String estimatedWeight, String cost, String averageWeight, String averageGross, String averageNet) {   
        Map<String, String> params = [CONO: company, DIVI: division, DPID: deckID, LOAD: load, DLOG: log, GVBF: grossVolume, NVBF: netVolume, ESWT: estimatedWeight, TCOI: cost, AWBF: averageWeight, GBFL: averageGross, NBFL: averageNet] 
        Closure<?> callback = {
          Map<String, String> response ->
        }
        
        miCaller.call("EXT005MI","UpdDeckDet", params, callback)        
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
    
    logger.info("In qry DPID ${DPID}")
    logger.info("In qry TRNO ${TRNO}")

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
  void addEXTDPRRecord(Integer CONO, String DIVI, int DPID, int TRNO, String SPEC, String TREF, int INBN, int TRDT, int TRTP, String ACCD, String ACNM, double LOAD, double TLOG, double TGBF, double TNBF, double TRNB, int AUWE, double GRWE, double TRWE, double NEWE){     
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
       EXTDPR.set("EXCHID", program.getUser())
       EXTDPR.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDPR.set("EXRGDT", regdate) 
       EXTDPR.set("EXLMDT", regdate) 
       EXTDPR.set("EXRGTM", regtime)
       action.insert(EXTDPR)         
 } 


  //******************************************************************** 
  // Update EXTDPD record
  //********************************************************************    
  void updEXTDPDRecord() {      
     DBAction action = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = action.getContainer()     
     EXTDPD.set("EXCONO", inCONO)
     EXTDPD.set("EXDIVI", inDIVI)
     EXTDPD.set("EXDPID", inDPID)

     // Read with lock
     action.readLock(EXTDPD, updateCallBackEXTDPD)
     }
   
     Closure<?> updateCallBackEXTDPD = { LockedResult lockedResult -> 
        lockedResult.set("EXLOAD", inLOADupdate)  
        lockedResult.set("EXDLOG", inTLOGupdate)
        lockedResult.set("EXGVBF", inTGBFupdate)
        lockedResult.set("EXNVBF", newTRNBadjust)
  
        // Update changed information
        int changeNo = lockedResult.get("EXCHNO")
        int newChangeNo = changeNo + 1 
        int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
        lockedResult.set("EXLMDT", changeddate)        
        lockedResult.set("EXCHNO", newChangeNo) 
        lockedResult.set("EXCHID", program.getUser())
        lockedResult.update()
     }

     
} 

