// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a new contract brand to EXTCTI
// Transaction AddContrInst
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: INIC - Instruction Code
 * @param: DPOR - Display Order
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CIID - Instruction ID
 * 
**/


public class AddContrInst extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inCIID
  boolean numberFound
  Integer lastNumber

  
  // Constructor 
  public AddContrInst(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
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
     String inINIC
     if (mi.in.get("INIC") != null) {
        inINIC = mi.inData.get("INIC").trim() 
     } else {
        inINIC = ""        
     }
     
     // Display Order
     int inDPOR
     if (mi.in.get("DPOR") != null) {
        inDPOR = mi.in.get("DPOR") 
     } else {
        inDPOR = 0       
     }
     
     
     // Validate contract instruction code record
     Optional<DBContainer> EXTCTI = findEXTCTI(inCONO, inDIVI, inRVID, inINIC, inDPOR)
     if(EXTCTI.isPresent()){
        mi.error("Contract Instruction already exists")   
        return             
     } else {
        findLastNumber()
        inCIID = lastNumber + 1
        // Write record 
        addEXTCTIRecord(inCONO, inDIVI, inRVID, inINIC, inDPOR, inCIID)          
     }  

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CIID", String.valueOf(inCIID))      
     mi.write()

  }
  
   //******************************************************************** 
   // Find last id number used
   //********************************************************************  
   void findLastNumber(){   
     
     numberFound = false
     lastNumber = 0

     ExpressionFactory expression = database.getExpressionFactory("EXTCTI")
     
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXDIVI", inDIVI))
     
     // Get Last Number
     DBAction actionline = database.table("EXTCTI")
     .index("20")
     .matching(expression)
     .selection("EXCIID")
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
        lastNumber = line.get("EXCIID") 
        numberFound = true
      }
  
  }
    
    
  //******************************************************************** 
  // Get EXTCTI record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTI(int CONO, String DIVI, String RVID, String INIC, int DPOR){  
     DBAction query = database.table("EXTCTI").index("00").build()
     DBContainer EXTCTI = query.getContainer()
     EXTCTI.set("EXCONO", CONO)
     EXTCTI.set("EXDIVI", DIVI)
     EXTCTI.set("EXRVID", RVID)
     EXTCTI.set("EXINIC", INIC)
     EXTCTI.set("EXDPOR", DPOR)
     if(query.read(EXTCTI))  { 
       return Optional.of(EXTCTI)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTCTI record 
  //********************************************************************     
  void addEXTCTIRecord(int CONO, String DIVI, String RVID, String INIC, int DPOR, int CIID){     
       DBAction action = database.table("EXTCTI").index("00").build()
       DBContainer EXTCTI = action.createContainer()
       EXTCTI.set("EXCONO", CONO)
       EXTCTI.set("EXDIVI", DIVI)
       EXTCTI.set("EXRVID", RVID)
       EXTCTI.set("EXINIC", INIC)
       EXTCTI.set("EXDPOR", DPOR)
       EXTCTI.set("EXCIID", CIID)   
       EXTCTI.set("EXCHID", program.getUser())
       EXTCTI.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTI.set("EXRGDT", regdate) 
       EXTCTI.set("EXLMDT", regdate) 
       EXTCTI.set("EXRGTM", regtime)
       action.insert(EXTCTI)         
 } 
     
} 

