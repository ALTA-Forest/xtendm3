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
  double detailLOAD
  double detailDLOG
  double detailGVBF
  double detailNVBF
  int automaticVolume
  BigDecimal transactionLOAD
  BigDecimal transactionTNBF
  BigDecimal transactionTGBF
  BigDecimal transactionTLOG
  BigDecimal transactionLOADrounded
  BigDecimal transactionTNBFrounded
  BigDecimal transactionTGBFrounded
  BigDecimal transactionTLOGrounded
  BigDecimal averageLOAD
  BigDecimal averageTNBF
  BigDecimal averageTGBF
  BigDecimal averageTLOG
  BigDecimal averageLOADrounded
  BigDecimal averageTNBFrounded
  BigDecimal averageTGBFrounded
  BigDecimal averageTLOGrounded
  String inDPIDString
  String inCONOString
  String inLOADString
  String inTLOGString
  String inTGBFString
  String inTNBFString
  String inESWTString
  String inTCOIString
  String inAWBFString
  String inGBFLString
  String inNBFLString
  String nextNumber
  boolean foundRecord
  String transactionDate
  String transactionNumber
  int transactionType
  String species
  String purchaseOrderNumber
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
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Deck ID
     int inDPID 
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0        
     }
  
     // Transaction Date
     int inTRDT 
     if (mi.in.get("TRDT") != null) {
        inTRDT = mi.in.get("TRDT") 
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
      getNextNumber("", "LP", "1") 
      outTRNO = nextNumber as Integer
      inTRNO = outTRNO as Integer

      //Get the record with most species for a specific deck from EXTDPR
      foundRecord = false
      listEXTDPRbySPEC()      
       
      averageLOAD = 0d
      averageTLOG = 0d
      averageTGBF = 0d
      averageTNBF = 0d
      
      //Calculate averageTNBF
      averageLOAD = inLOAD/detailLOAD
      averageTLOG = inTLOG/detailDLOG
      averageTGBF = inTGBF/detailGVBF
      averageTNBF = inTNBF/detailNVBF
           
      transactionLOAD = 0d
      transactionTNBF = 0d
      transactionTGBF = 0d
      transactionTLOG = 0d
      automaticVolume = 0
      
      if (inISPC == 0) {
         if (inTNBF > 0d) {
            transactionTNBF = inTNBF
            transactionTGBF = detailGVBF * averageTNBF
            transactionTLOG = detailDLOG * averageTNBF
            transactionLOAD = detailLOAD * averageTNBF
         } else if (inTGBF > 0d) {
            transactionTNBF = detailNVBF * averageTGBF
            transactionTGBF = inTGBF
            transactionTLOG = detailDLOG * averageTGBF
            transactionLOAD = detailLOAD * averageTGBF
         } else if (inTLOG > 0d) {
            transactionTNBF = detailNVBF * averageTLOG
            transactionTGBF = detailGVBF * averageTLOG
            transactionTLOG = inTLOG
            transactionLOAD = detailLOAD * averageTLOG
         } else if (inLOAD> 0d) {
            transactionTNBF = detailNVBF * averageLOAD
            transactionTGBF = detailGVBF * averageLOAD
            transactionTLOG = detailDLOG * averageLOAD
            transactionLOAD = inLOAD
         }
      } else if (inISPC == 1) {
            transactionTNBF = detailNVBF * (inPCTG/100)
            transactionTGBF = detailGVBF * (inPCTG/100)
            transactionTLOG = detailDLOG * (inPCTG/100)
            transactionLOAD = detailLOAD * (inPCTG/100)
      }
      
      transactionLOAD = transactionLOAD.setScale(0, RoundingMode.HALF_UP)   
      transactionTNBF = transactionTNBF.setScale(2, RoundingMode.HALF_UP)   
      transactionTGBF = transactionTGBF.setScale(2, RoundingMode.HALF_UP)   
      transactionTLOG = transactionTLOG.setScale(0, RoundingMode.HALF_UP) 

      double transactionTNBFadjust = transactionTNBF
      double transactionTGBFadjust = transactionTGBF
      double transactionTLOGadjust = transactionTLOG
      double transactionLOADadjust = transactionLOAD    
      
      species = ""
      purchaseOrderNumber = ""
      invoiceBatchNumber = 0
      accountCode = ""
      accountName = ""
      
      if (inEINV == 0) {
         addEXTDPRRecord(inCONO, inDIVI, inDPID, inTRNO, species, purchaseOrderNumber, invoiceBatchNumber, inTRDT, 3, accountCode, accountName, -transactionLOADadjust, -transactionTLOGadjust, -transactionTGBFadjust, -transactionTNBFadjust, automaticVolume, -grossWeight, -tareWeight, -netWeight)           
      } else if (inEINV == 1) {
         addEXTDPRRecord(inCONO, inDIVI, inDPID, inTRNO, species, purchaseOrderNumber, invoiceBatchNumber, inTRDT, 3, accountCode, accountName, transactionLOADadjust, transactionTLOGadjust, transactionTGBFadjust, transactionTNBFadjust, automaticVolume, grossWeight, tareWeight, netWeight)   
      }
      
      inLOADString = ""
      inTLOGString = ""
      inTGBFString = ""
      inTNBFString = ""
      
      //Adjust deck details
      inCONOString = String.valueOf(inCONO)
      inDPIDString = String.valueOf(inDPID)
      
      if (inEINV == 0) {
        inLOADString = String.valueOf(detailLOAD-transactionLOADadjust)
        inTLOGString = String.valueOf(detailDLOG-transactionTLOGadjust)
        inTGBFString = String.valueOf(detailGVBF-transactionTGBFadjust)
        inTNBFString = String.valueOf(detailNVBF-transactionTNBFadjust)
      } else if (inEINV == 1) {
        inLOADString = String.valueOf(detailLOAD+transactionLOADadjust)
        inTLOGString = String.valueOf(detailDLOG+transactionTLOGadjust)
        inTGBFString = String.valueOf(detailGVBF+transactionTGBFadjust)
        inTNBFString = String.valueOf(detailNVBF+transactionTNBFadjust)        
      }
      updDeckDetailsMI(inCONOString, inDIVI, inDPIDString, inLOADString, inTLOGString, inTGBFString, inTNBFString, inESWTString, inTCOIString, inAWBFString, inGBFLString, inNBFLString)   

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
    purchaseOrderNumber = container.getString("EXPUNO")
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
  // Add EXTDPR record 
  //********************************************************************     
  void addEXTDPRRecord(Integer CONO, String DIVI, int DPID, int TRNO, String SPEC, String PUNO, int INBN, int TRDT, int TRTP, String ACCD, String ACNM, double LOAD, double TLOG, double TGBF, double TNBF, int AUWE, double GRWE, double TRWE, double NEWE){     
       DBAction action = database.table("EXTDPR").index("00").build()
       DBContainer EXTDPR = action.createContainer()
       EXTDPR.set("EXCONO", CONO)
       EXTDPR.set("EXDIVI", DIVI)
       EXTDPR.set("EXDPID", DPID)
       EXTDPR.set("EXTRNO", TRNO)
       EXTDPR.set("EXSPEC", SPEC)
       EXTDPR.set("EXPUNO", PUNO)
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

     
} 

