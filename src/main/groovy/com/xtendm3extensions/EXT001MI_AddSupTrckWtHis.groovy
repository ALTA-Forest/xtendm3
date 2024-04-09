// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add Supplier Truck Weight History to EXTSTR
// Transaction AddSupTrckWtHis
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: TARE - TARE
 * @param: FRDT - From Date
 * @param: TODT - To Date
 * 
*/


public class AddSupTrckWtHis extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  int inCONO
  String inSUNO
  String inTRCK
  int inTARE
  int inFRDT
  int inTODT
  boolean dateNotValid
  
  // Constructor 
  public AddSupTrckWtHis(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Supplier
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim()
        
        // Validate supplier if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inSUNO)
        if (!CIDMAS.isPresent()) {
           mi.error("Supplier doesn't exist")   
           return             
        }

     } else {
        inSUNO = ""         
     }
      
     // Truck
     if (mi.in.get("TRCK") != null && mi.in.get("TRCK") != "") {
        inTRCK = mi.inData.get("TRCK").trim() 
     } else {
        inTRCK = ""        
     }
     
    // Tare
     if (mi.in.get("TARE") != null) {
        inTARE = mi.in.get("TARE") 
     } else {
        inTARE = 0        
     }
     
     // From Date
     if (mi.in.get("FRDT") != null) {
        inFRDT = mi.in.get("FRDT") 
        
        //Validate date format
        boolean validFRDT = utility.call("DateUtil", "isDateValid", String.valueOf(inFRDT), "yyyyMMdd")  
        if (!validFRDT) {
           mi.error("From Date is not valid")   
           return  
        } 

     } else {
        inFRDT = 0        
     }

     // To Date
     if (mi.in.get("TODT") != null) {
        inTODT = mi.in.get("TODT") 
        
        //Validate date format
        boolean validTODT = utility.call("DateUtil", "isDateValid", String.valueOf(inTODT), "yyyyMMdd")  
        if (!validTODT) {
           mi.error("To Date is not valid")   
           return  
        } 

     } else {
        inTODT = 0        
     }

     if (inFRDT > inTODT) {
        mi.error("From Date can not be after To Date")   
        return             
     }

     // Validate supplier truck weight history record
     Optional<DBContainer> EXTTWH = findEXTTWH(inCONO, inSUNO, inTRCK, inFRDT, inTODT)
     if(EXTTWH.isPresent()){
        mi.error("Supplier Truck Weight History already exists")   
        return             
     } 
     
     validateInputDates()
     
     if (dateNotValid) { 
        mi.error("Date Range is not valid")           
        return             
     } else {
        // Write record 
        addEXTTWHRecord(inCONO, inSUNO, inTRCK, inTARE, inFRDT, inTODT)          
     }     

  }
  
  
   //******************************************************************** 
   // Validate the date is not overlapping
   //********************************************************************  
   void validateInputDates(){   
     
     dateNotValid = false

     ExpressionFactory expression = database.getExpressionFactory("EXTTWH")
   
     expression = expression.eq("EXCONO", String.valueOf(inCONO)).and(expression.eq("EXSUNO", inSUNO)).and(expression.eq("EXTRCK", inTRCK))
     
     // Get Supplier/Truck records
     DBAction actionline = database.table("EXTTWH")
     .index("00")
     .matching(expression)
     .selection("EXFRDT", "EXTODT")
     .build()

     DBContainer line = actionline.getContainer()  
             
     actionline.readAll(line, 0, releasedLineProcessor)   
   
   } 

    
  //******************************************************************** 
  // Check dates - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line -> 

      // Output
      int fromDate = line.get("EXFRDT") 
      int toDate = line.get("EXTODT") 
      
      if (((inFRDT >= fromDate) && (inFRDT <= toDate)) || ((inTODT >= fromDate) && (inTODT <= toDate)) ) {
         mi.error("Date Range is not valid")   
         dateNotValid = true
         return             
      }
  
      if ((inFRDT < fromDate) && (inTODT > toDate) ) {
         mi.error("Date Range is not valid")   
         dateNotValid = true
         return             
      }
  }
  
  //******************************************************************** 
  // Get EXTTWH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTTWH(int cono, String suno, String trck, int frdt, int todt){  
     DBAction query = database.table("EXTTWH").index("00").build()
     DBContainer EXTTWH = query.getContainer()
     EXTTWH.set("EXCONO", cono)
     EXTTWH.set("EXSUNO", suno)
     EXTTWH.set("EXTRCK", trck)
     EXTTWH.set("EXFRDT", frdt)
     EXTTWH.set("EXTODT", todt)
     if(query.read(EXTTWH))  { 
       return Optional.of(EXTTWH)
     } 
  
     return Optional.empty()
  }


   //******************************************************************** 
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }

  
  //******************************************************************** 
  // Add EXTTWH record 
  //********************************************************************     
  void addEXTTWHRecord(int cono, String suno, String trck, int tare, int frdt, int todt){     
       DBAction action = database.table("EXTTWH").index("00").build()
       DBContainer EXTTWH = action.createContainer()
       EXTTWH.set("EXCONO", cono)
       EXTTWH.set("EXSUNO", suno)
       EXTTWH.set("EXTRCK", trck)
       EXTTWH.set("EXTARE", tare)
       EXTTWH.set("EXFRDT", frdt)
       EXTTWH.set("EXTODT", todt)
       EXTTWH.set("EXCHID", program.getUser())
       EXTTWH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTTWH.set("EXRGDT", regdate) 
       EXTTWH.set("EXLMDT", regdate) 
       EXTTWH.set("EXRGTM", regtime)
       action.insert(EXTTWH)         
 } 

     
} 

