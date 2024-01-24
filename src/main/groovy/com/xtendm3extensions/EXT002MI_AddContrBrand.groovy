// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a new contract brand to EXTCTB
// Transaction AddContrBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: BRND - Brand
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CBID - Brand ID
 * 
*/


public class AddContrBrand extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inCBID
  boolean numberFound
  Integer lastNumber
  
  // Constructor 
  public AddContrBrand(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Revision ID
     String inRVID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""         
     }
      
     // Brand
     String inBRND
     if (mi.in.get("BRND") != null) {
        inBRND = mi.inData.get("BRND").trim() 
     } else {
        inBRND = ""        
     }
     

     // Validate contract brand record
     Optional<DBContainer> EXTCTB = findEXTCTB(inCONO, inDIVI, inRVID, inBRND)
     if(EXTCTB.isPresent()){
        mi.error("Contract Brand already exists")   
        return             
     } else {
        findLastNumber()
        inCBID = lastNumber + 1
        // Write record 
        addEXTCTBRecord(inCONO, inDIVI, inRVID, inBRND, inCBID)  
     } 
     
     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CBID", String.valueOf(inCBID))     
     mi.write()

  }
  

   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTCTB")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTCTB")
     .index("10")
     .matching(expression)
     .selection("EXCBID")
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
        lastNumber = line.get("EXCBID") 
        numberFound = true
      }
  
  }
  
    
  //******************************************************************** 
  // Get EXTCTB record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTB(int CONO, String DIVI, String RVID, String BRND){  
     DBAction query = database.table("EXTCTB").index("00").build()
     DBContainer EXTCTB = query.getContainer()
     EXTCTB.set("EXCONO", CONO)
     EXTCTB.set("EXDIVI", DIVI)
     EXTCTB.set("EXRVID", RVID)
     EXTCTB.set("EXBRND", BRND)
     if(query.read(EXTCTB))  { 
       return Optional.of(EXTCTB)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCTB record 
  //********************************************************************     
  void addEXTCTBRecord(int CONO, String DIVI, String RVID, String BRND, int CBID){     
       DBAction action = database.table("EXTCTB").index("00").build()
       DBContainer EXTCTB = action.createContainer()
       EXTCTB.set("EXCONO", CONO)
       EXTCTB.set("EXDIVI", DIVI)
       EXTCTB.set("EXRVID", RVID)
       EXTCTB.set("EXBRND", BRND)
       EXTCTB.set("EXCBID", CBID)  
       EXTCTB.set("EXCHID", program.getUser())
       EXTCTB.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTB.set("EXRGDT", regdate) 
       EXTCTB.set("EXLMDT", regdate) 
       EXTCTB.set("EXRGTM", regtime)
       action.insert(EXTCTB)         
 } 

     
} 

