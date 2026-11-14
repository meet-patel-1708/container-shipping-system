import { AbstractControl,ValidationErrors,ValidatorFn } from "@angular/forms";

export const PASSWORD_RULES=[
    {key:'length',label:'At least 8 characters',test:(v:string)=>v.length>=8},
    {key:'lower',label:'One lowercase character',test:(v:string)=>/[a-z]/.test(v)},
    {key:'upper',label:'One uppercase character',test:(v:string)=>/[A-Z]/.test(v)},
    {key:'digit',label:'one number',test:(v:string)=>/\d/.test(v)},
    {key:'special',label:'One special character(@$!%*?&#)',test:(v:string)=>/[@$!%*?&#]/.test(v)},
]as const;

export const passwordStrength:ValidatorFn=(control:AbstractControl):ValidationErrors|null=>{
    const value =(control.value??'') as string;
    if(!value)return null;
    const failed = PASSWORD_RULES.filter((r)=>!r.test(value)).map((r)=>r.key);
    return failed.length?{passwordStrength:failed}:null;
};

export function matchFields(field:string,confirmField:string):ValidatorFn{
    return(group:AbstractControl):ValidationErrors|null=>{
        const a = group.get(field)?.value;
    const b = group.get(confirmField)?.value;
    return a && b && a !== b ? { mismatch: true } : null;
    };
}
export const indianPhone: ValidatorFn = (control) =>
  !control.value || /^[6-9]\d{9}$/.test(control.value) ? null : { indianPhone: true };