// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add contract section to EXTDPH
// Transaction AddDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

// Date         Changed By                         Description
// 2023-04-10   Jessica Bjorklund (Columbus)       Creation
// 2024-10-17   Jessica Bjorklund (Columbus)       Allow blank input for Strings & add logging
// 2024-10-28   Jessica Bjorklund (Columbus)       Change input field DPNA to 100 long

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPNA - Deck Name
 * @param: TYPE - Deck Type
 * @param: SORT - Sort Code
 * @param: YARD - Yard
 * @param: MBFW - Weight of 1 MBF
 * @param: DPDT - Deck Date
 * @param: DPLC - Life Cycle
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DPID - Deck ID
 * 
**/


public class AddDeck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDPID
  boolean numberFound
  Integer lastNumber

  
  // Constructor 
  public AddDeck(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger
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

     // Deck Name
     String inDPNA
     if (mi.in.get("DPNA") != null) {
        inDPNA = mi.inData.get("DPNA").trim() 
     } else {
        inDPNA = ""         
     }

     // Deck Type
     String inTYPE  
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.inData.get("TYPE").trim() 
     } else {
        inTYPE = ""      
     }
           
     // Sort Code
     String inSORT  
     if (mi.in.get("SORT") != null) {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT= ""      
     }
     
     // Validate Sort Code
     Optional<DBContainer> EXTSOR = findEXTSOR(inCONO, inSORT)
     if(!EXTSOR.isPresent()){
        mi.error("Sort Code doesn't exist")   
        return             
     }

     // Yard
     String inYARD  
     if (mi.in.get("YARD") != null) {
        inYARD = mi.inData.get("YARD").trim() 
     } else {
        inYARD = ""      
     }

     // Weight of 1 MBF
     double inMBFW  
     if (mi.in.get("MBFW") != null) {
        inMBFW = mi.in.get("MBFW") 
     } else {
        inMBFW = 0d        
     }

     // Deck Date
     int inDPDT  
     if (mi.in.get("DPDT") != null) {
        inDPDT = mi.in.get("DPDT") 
        
        //Validate date format
        boolean validDPDT = utility.call("DateUtil", "isDateValid", String.valueOf(inDPDT), "yyyyMMdd")  
        if (!validDPDT) {
           mi.error("Deck Date is not valid")   
           return  
        } 

     } else {
        inDPDT = 0        
     }
     
     // Life Cycle
     int inDPLC  
     if (mi.in.get("DPLC") != null) {
        inDPLC = mi.in.get("DPLC") 
     } else {
        inDPLC = 0        
     }

     //Get next Deck ID
     findLastNumber()
     inDPID = lastNumber + 1


     // Validate Deck Profile Head record
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if(EXTDPH.isPresent()){
        mi.error("Deck Profile already exists")   
        return             
     } else {
        logger.debug("Add to EXTDLH inDPID ${inDPID}")
        logger.debug("Add to EXTDLH inDPNA ${inDPNA}")
        logger.debug("Add to EXTDLH inTYPE ${inTYPE}")
        logger.debug("Add to EXTDLH inSORT ${inSORT}")
        logger.debug("Add to EXTDLH inYARD ${inYARD}")
        logger.debug("Add to EXTDLH inMBFW ${inMBFW}")
        logger.debug("Add to EXTDLH inDPDT ${inDPDT}")
        logger.debug("Add to EXTDLH inDPLC ${inDPLC}") 
        
        // Write record     
        addEXTDPHRecord(inCONO, inDIVI, inDPID, inDPNA, inTYPE, inSORT, inYARD, inMBFW, inDPDT, inDPLC)   
        addDeckDetailsMI(String.valueOf(inCONO), inDIVI, String.valueOf(inDPID), "", "", "", "", "", "", "", "", "")   
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("DPID", String.valueOf(inDPID))      
     mi.write()
  }
  
  
   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTDPH")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTDPH")
     .index("00")
     .matching(expression)
     .selection("EXDPID")
     .reverse()
     .build()

     DBContainer line = actionline.getContainer() 
     
     line.set("EXCONO", inCONO)     
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)      
   } 

    
  //******************************************************************** 
  // List Last Number
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 

      // Output
      if (!lastNumber) {
        lastNumber = line.get("EXDPID") 
        numberFound = true
      }
  
  }


   //***************************************************************************** 
   // Add Deck Details
   //***************************************************************************** 
   void addDeckDetailsMI(String company, String division, String deckID, String load, String log, String grossVolume, String netVolume, String estimatedWeight, String cost, String averageWeight, String averageGross, String averageNet) {   
        Map<String, String> params = [CONO: company, DIVI: division, DPID: deckID, LOAD: load, DLOG: log, GVBF: grossVolume, NVBF: netVolume, ESWT: estimatedWeight, TCOI: cost, AWBF: averageWeight, GBFL: averageGross, NBFL: averageNet] 
        Closure<?> callback = {
          Map<String, String> response ->
        }
        
        miCaller.call("EXT005MI","AddDeckDet", params, callback)
   } 


  //******************************************************************** 
  // Get EXTDPH record
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


  //******************************************************************** 
  // Validate EXTSOR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSOR(int CONO, String SORT){  
     DBAction query = database.table("EXTSOR").index("00").build()
     DBContainer EXTSOR = query.getContainer()
     EXTSOR.set("EXCONO", CONO)
     EXTSOR.set("EXSORT", SORT)
     if(query.read(EXTSOR))  { 
       return Optional.of(EXTSOR)
     } 
  
     return Optional.empty()
  }

  
  //******************************************************************** 
  // Add EXTDPH record 
  //********************************************************************     
  void addEXTDPHRecord(int CONO, String DIVI, int DPID, String DPNA, String TYPE, String SORT, String YARD, double MBFW, int DPDT, int DPLC){     
       DBAction action = database.table("EXTDPH").index("00").build()
       DBContainer EXTDPH = action.createContainer()
       EXTDPH.set("EXCONO", CONO)
       EXTDPH.set("EXDIVI", DIVI)
       EXTDPH.set("EXDPID", DPID)       
       EXTDPH.set("EXDPNA", DPNA)
       EXTDPH.set("EXTYPE", TYPE)
       EXTDPH.set("EXSORT", SORT)
       EXTDPH.set("EXYARD", YARD)
       EXTDPH.set("EXMBFW", MBFW)
       EXTDPH.set("EXDPDT", DPDT)
       EXTDPH.set("EXDPLC", DPLC)   
       EXTDPH.set("EXCHID", program.getUser())
       EXTDPH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDPH.set("EXRGDT", regdate) 
       EXTDPH.set("EXLMDT", regdate) 
       EXTDPH.set("EXRGTM", regtime)
       action.insert(EXTDPH)         
 } 

     
} 

