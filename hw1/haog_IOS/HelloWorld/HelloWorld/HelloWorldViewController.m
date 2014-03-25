//
//  HelloWorldViewController.m
//  HelloWorld
//
//  Created by Hao Gao on 3/25/14.
//  Copyright (c) 2014 Hao Gao. All rights reserved.
//

#import "HelloWorldViewController.h"

@interface HelloWorldViewController ()

@end

@implementation HelloWorldViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)changeText:(id)sender {
    NSString *txt = self.inputTextField.text;
    NSString *str = @"Welcome ";
    str = [str stringByAppendingString:txt];
    str = [str stringByAppendingString:@" !"];
    self.welcomeLabel.text = str;
    [self.view endEditing:YES];
}
@end
